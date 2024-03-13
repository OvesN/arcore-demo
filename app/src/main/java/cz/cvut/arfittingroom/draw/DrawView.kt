package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.actions
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.lastPaths
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.paths
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.undonePaths
import cz.cvut.arfittingroom.draw.command.action.DrawPath
import cz.cvut.arfittingroom.draw.model.element.Star
import cz.cvut.arfittingroom.draw.model.enums.EShape
import cz.cvut.arfittingroom.draw.path.DrawablePath
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

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
    private val matrix = Matrix()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    private var imageBitmap: Bitmap? = null

    var isInImageMode = false
    var strokeShape = EShape.CIRCLE

    init {
        curPaint.apply {
            color = paintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = paintOptions.strokeWidth
            isAntiAlias = true
        }

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

        actions.forEach{it.execute(canvas)}

//        if (imageBitmap != null) {
//            matrix.reset()
//            matrix.postTranslate(-imageBitmap!!.width / 2f, -imageBitmap!!.height / 2f)
//            matrix.postScale(scaleFactor, scaleFactor)
//            matrix.postTranslate(posX + imageBitmap!!.width / 2f, posY + imageBitmap!!.height / 2f)
//        }
//
//        imageBitmap?.let {
//            canvas.drawBitmap(it, matrix, null)
//        }
//
//        for ((key, value) in paths) {
//            changePaint(value)
//            canvas.drawPath(key, paint)
//        }
//
//
//        changePaint(paintOptions)
//        canvas.drawPath(path, paint)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        curPaint.color = paintOptions.color
        curPaint.style = paintOptions.style
        curPaint.strokeWidth = paintOptions.strokeWidth
    }

    fun clearCanvas() {
        paths.putAll(lastPaths.clone() as LinkedHashMap<DrawablePath, PaintOptions>)
        curPath.reset()
        paths.clear()
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

        paths[curPath] = paintOptions
        curPath = DrawablePath()
        paintOptions =
            PaintOptions(paintOptions.color, paintOptions.strokeWidth, paintOptions.alpha)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (isInImageMode) {
            adjustImage(event)
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
                        undonePaths.clear()
                        invalidate()
                        return true
                    }

                    else -> {
                        startX = x
                        startY = y
                        actionDown(x, y)
                        undonePaths.clear()
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }

        invalidate()
        return true
    }

    private fun drawHeart(centerX: Float, centerY: Float, outerRadius: Float) {
        val heartPath = createHeartPath(centerX, centerY, outerRadius)
        val newPaintOptions =
            PaintOptions(paintOptions.color, outerRadius, paintOptions.alpha, Paint.Style.FILL)
        paths[heartPath] = newPaintOptions
    }

    private fun drawStar(centerX: Float, centerY: Float, outerRadius: Float) {
        val star = Star(
            centerX,
            centerY,
            outerRadius,
            Paint().apply {
                color = paintOptions.color
                style = paintOptions.style
                strokeWidth = paintOptions.strokeWidth
            }
        )

        actions.add(DrawPath(star))
    }


    private fun createHeartPath(cx: Float, cy: Float, outerRadius: Float): DrawablePath {
        val path = DrawablePath()
        // Starting point
        path.moveTo(outerRadius / 2 + cx, outerRadius / 5 + cy)

        // Upper left path
        path.cubicTo(
            5 * outerRadius / 14 + cx, cy,
            cx, outerRadius / 15 + cy,
            outerRadius / 28 + cx, 2 * outerRadius / 5 + cy
        )

        // Lower left path
        path.cubicTo(
            outerRadius / 14 + cx, 2 * outerRadius / 3 + cy,
            3 * outerRadius / 7 + cx, 5 * outerRadius / 6 + cy,
            outerRadius / 2 + cx, outerRadius + cy
        )

        // Lower right path
        path.cubicTo(
            4 * outerRadius / 7 + cx, 5 * outerRadius / 6 + cy,
            13 * outerRadius / 14 + cx, 2 * outerRadius / 3 + cy,
            27 * outerRadius / 28 + cx, 2 * outerRadius / 5 + cy
        )

        // Upper right path
        path.cubicTo(
            outerRadius + cx, outerRadius / 15 + cy,
            9 * outerRadius / 14 + cx, 0f + cy,
            outerRadius / 2 + cx, outerRadius / 5 + cy
        )

        return path
    }


    fun drawImage(image: Int) {
        imageBitmap = BitmapFactory.decodeResource(resources, image)
        invalidate()
    }

}