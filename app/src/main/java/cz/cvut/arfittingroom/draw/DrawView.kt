package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.actions
import cz.cvut.arfittingroom.draw.command.action.DrawPath
import cz.cvut.arfittingroom.draw.model.element.impl.Curve
import cz.cvut.arfittingroom.draw.model.element.impl.Heart
import cz.cvut.arfittingroom.draw.model.element.impl.Star
import cz.cvut.arfittingroom.draw.model.enums.EShape
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.service.LayerManager
import cz.cvut.arfittingroom.service.MakeupService
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger { }

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    @Inject
    lateinit var layerManager: LayerManager

    private var curDrawingPath = LinkedHashMap<DrawablePath, PaintOptions>()
    private var curPaint = Paint()
    private var curPath = DrawablePath()
    private var paintOptions = PaintOptions()

    private var curX = 0f
    private var curY = 0f
    private var startX = 0f
    private var startY = 0f
    private var isSaving = false
    private var isStrokeWidthBarEnabled = false

    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    private var imageBitmap: Bitmap? = null

    private var activeLayerIndex = 0


    var isInImageMode = false
    var strokeShape = EShape.CIRCLE

    init {
        // Apply default setting for paint option
        curPaint.apply {
            color = paintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = paintOptions.strokeWidth
            isAntiAlias = true
        }

        // Add event for image scaling
        scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = 0.1f.coerceAtLeast(scaleFactor.coerceAtMost(10.0f))
                    invalidate()
                    return true
                }
            })

        // Initialize first layer
        layerManager.addLayer(width, height)
    }

    private fun adjustImage(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scaleGestureDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    posX += dx
                    posY += dy
                    invalidate()
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
        }
        return true
    }

    fun undo() {
        DrawHistoryHolder.undo()
        invalidate()
    }

    fun redo() {
        DrawHistoryHolder.redo()
        invalidate()
    }

    fun setColor(newColor: Int) {
        @ColorInt
        paintOptions.color = newColor
        paintOptions.alpha = newColor.alpha
        if (isStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        paintOptions.strokeWidth = newStrokeWidth
        if (isStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        isSaving = true
        draw(canvas)
        isSaving = false
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw all layers
        layerManager.drawLayers(canvas)

        // Draw the current path that the user is drawing
        curDrawingPath.forEach{
            changePaint(it.value)
            canvas.drawPath(it.key, curPaint)
        }

        // Draw the current part of the part that the user is drawing
        changePaint(paintOptions)
        canvas.drawPath(curPath, curPaint)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        curPaint.color = paintOptions.color
        curPaint.style = paintOptions.style
        curPaint.strokeWidth = paintOptions.strokeWidth
    }

    fun clearCanvas() {
        curPath.reset()
        actions.clear()
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        curPath.reset()
        curPath.moveTo(x, y)
        curX = x
        curY = y
    }

    private fun actionMove(x: Float, y: Float) {
        curPath.quadTo(curX, curY, (x + curX) / 2, (y + curY) / 2)
        curX = x
        curY = y
    }

    private fun actionUp() {
        curPath.lineTo(curX, curY)

        // draw a dot on click
        if (startX == curX && startY == curY) {
            curPath.lineTo(curX, curY + 2)
            curPath.lineTo(curX + 1, curY + 2)
            curPath.lineTo(curX + 1, curY)
        }

        val curve = Curve(curPath, Paint().apply {
            color = paintOptions.color
            strokeWidth = paintOptions.strokeWidth
            alpha = paintOptions.alpha
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        })

        actions.add(DrawPath(curve))

        curPath = DrawablePath()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (isInImageMode) {
            adjustImage(event)
            invalidate()
            return true
        }

        if (strokeShape == EShape.CIRCLE) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = x
                    startY = y
                    actionDown(x, y)
                }

                MotionEvent.ACTION_MOVE -> actionMove(x, y)
                MotionEvent.ACTION_UP -> actionUp()
            }
            invalidate()
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (strokeShape) {
                    EShape.STAR -> {
                        drawStar(x, y, paintOptions.strokeWidth)
                        invalidate()
                        return true
                    }

                    EShape.HEART -> {
                        drawHeart(x, y, paintOptions.strokeWidth)
                        invalidate()
                        return true
                    }

                    else -> {}
                }
            }
        }

        invalidate()
        return true
    }

    private fun drawHeart(centerX: Float, centerY: Float, outerRadius: Float) {
        val heart = Heart(
            centerX,
            centerY,
            outerRadius,
            Paint().apply {
                color = paintOptions.color
                strokeWidth = outerRadius
                alpha = paintOptions.alpha
                style = Paint.Style.FILL
            }
        )
        actions.add(DrawPath(heart))
    }

    private fun drawStar(centerX: Float, centerY: Float, outerRadius: Float) {
        val star = Star(
            centerX,
            centerY,
            outerRadius,
            Paint().apply {
                color = paintOptions.color
                style = paintOptions.style
                strokeWidth = 6f
                alpha = paintOptions.alpha
            }
        )
        actions.add(DrawPath(star))
    }

    fun drawImage(image: Int) {
        imageBitmap = BitmapFactory.decodeResource(resources, image)
        invalidate()
    }

}