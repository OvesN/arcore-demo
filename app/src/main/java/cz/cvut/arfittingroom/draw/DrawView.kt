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
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.globalDrawHistory
import cz.cvut.arfittingroom.draw.command.action.DrawPath
import cz.cvut.arfittingroom.draw.model.element.impl.Curve
import cz.cvut.arfittingroom.draw.model.element.impl.Heart
import cz.cvut.arfittingroom.draw.model.element.impl.Star
import cz.cvut.arfittingroom.draw.model.enums.EShape
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.service.LayerManager
import mu.KotlinLogging
import javax.inject.Inject

private val logger = KotlinLogging.logger { }

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    @Inject
    lateinit var layerManager: LayerManager
    private var isLayerInitialized = false
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


    var isInImageMode = false
    private var isInElementEditMode = false
    var strokeShape = EShape.CIRCLE

    init {
        (context.applicationContext as? ARFittingRoomApplication)?.appComponent?.inject(this)

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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        //TODO fix this, different modes and different brushes?
        when (strokeShape) {
            EShape.NONE -> handleElementDeformationMode(event, x, y)
            EShape.CIRCLE -> handleDrawingMode(event, x, y)
            else -> handleStampMode(event, x, y)
        }

        invalidate()
        return true
    }

    private fun handleElementDeformationMode(event: MotionEvent, x: Float, y: Float) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            layerManager.selectElement(x, y)
        }
    }

    private fun handleDrawingMode(event: MotionEvent, x: Float, y: Float) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                actionDown(x, y)
            }

            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }
    }

    private fun handleStampMode(event: MotionEvent, x: Float, y: Float) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (strokeShape) {
                EShape.STAR -> drawStar(x, y, paintOptions.strokeWidth)
                EShape.HEART -> drawHeart(x, y, paintOptions.strokeWidth)
                else -> {}
            }
        }
    }

    fun undo() {
        layerManager.undo()
        invalidate()
    }

    fun redo() {
        layerManager.redo()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //Initialize first layer
        if (!isLayerInitialized) {
            layerManager.addLayer(w, h)
            isLayerInitialized = true
        }
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

    fun addLayer(): Int {
        val newLayerIndex = layerManager.addLayer(width, height)
        layerManager.activeLayerIndex = newLayerIndex
        return newLayerIndex
    }

    //Return true if new layer is selected
    fun setActiveLayer(layerIndex: Int): Boolean {
        if (layerIndex >= layerManager.getNumOfLayers() || layerIndex < 0) {
            return false
        }
        layerManager.activeLayerIndex = layerIndex
        logger.info { "Active layer is now $layerIndex" }
        return true
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
        layerManager.drawLayers(canvas, paintOptions)
    }

    fun clearCanvas() {
        layerManager.deleteLayers()
        globalDrawHistory.clear()
        // Add initial layer
        layerManager.addLayer(width, height)
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        layerManager.getCurPath().reset()
        layerManager.getCurPath().moveTo(x, y)
        curX = x
        curY = y
    }

    private fun actionMove(x: Float, y: Float) {
        layerManager.getCurPath().quadTo(curX, curY, (x + curX) / 2, (y + curY) / 2)
        curX = x
        curY = y
    }

    private fun actionUp() {
        layerManager.getCurPath().lineTo(curX, curY)

        // draw a dot on click
        if (startX == curX && startY == curY) {
            layerManager.getCurPath().lineTo(curX, curY + 2)
            layerManager.getCurPath().lineTo(curX + 1, curY + 2)
            layerManager.getCurPath().lineTo(curX + 1, curY)
        }

        val curve = Curve(layerManager.getCurPath(), Paint().apply {
            color = paintOptions.color
            strokeWidth = paintOptions.strokeWidth
            alpha = paintOptions.alpha
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        })

        layerManager.addToLayer(layerManager.activeLayerIndex, DrawPath(curve))

        layerManager.setCurPath(DrawablePath())
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
        layerManager.addToLayer(layerManager.activeLayerIndex, DrawPath(heart))
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
        layerManager.addToLayer(layerManager.activeLayerIndex, DrawPath(star))
    }

    fun drawImage(image: Int) {
        imageBitmap = BitmapFactory.decodeResource(resources, image)
        invalidate()
    }

    fun moveLayer(fromIndex: Int, toIndex: Int) {
        if (layerManager.moveLayer(fromIndex, toIndex)) {
            layerManager.activeLayerIndex = toIndex
            invalidate()
        }
    }

    fun removeLayer(layerIndex: Int) {
        layerManager.removeLayer(layerIndex)
        layerManager.activeLayerIndex = if (layerIndex == 0) 0 else layerIndex - 1
        invalidate()
    }

}