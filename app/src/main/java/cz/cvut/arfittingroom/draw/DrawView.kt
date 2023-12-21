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
import mu.KotlinLogging
import kotlin.math.cos
import kotlin.math.sin

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mPaths = LinkedHashMap<MyPath, PaintOptions>()
    private val logger = KotlinLogging.logger { }

    private var mLastPaths = LinkedHashMap<MyPath, PaintOptions>()
    private var mUndonePaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mPaint = Paint()
    private var mPath = MyPath()
    private var mPaintOptions = PaintOptions()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var mIsSaving = false
    private var mIsStrokeWidthBarEnabled = false

    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector
    private val matrix = Matrix()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    var imageBitmap: Bitmap? = null
    var isInImageMode = false

    var strokeShape = EShape.CIRCLE

    init {
        mPaint.apply {
            color = mPaintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mPaintOptions.strokeWidth
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

        val action = event.actionMasked
        when (action) {
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
        if (mPaths.isEmpty() && mLastPaths.isNotEmpty()) {
            mPaths = mLastPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
            mLastPaths.clear()
            invalidate()
            return
        }
        if (mPaths.isEmpty()) {
            return
        }
        val lastPath = mPaths.values.lastOrNull()
        val lastKey = mPaths.keys.lastOrNull()

        mPaths.remove(lastKey)
        if (lastPath != null && lastKey != null) {
            mUndonePaths[lastKey] = lastPath
        }
        invalidate()
    }

    fun redo() {
        if (mUndonePaths.keys.isEmpty()) {
            return
        }

        val lastKey = mUndonePaths.keys.last()
        addPath(lastKey, mUndonePaths.values.last())
        mUndonePaths.remove(lastKey)
        invalidate()
    }

    fun setColor(newColor: Int) {
        @ColorInt
        mPaintOptions.color = newColor
        mPaintOptions.alpha = newColor.alpha
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        mPaintOptions.strokeWidth = newStrokeWidth
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        mIsSaving = true
        draw(canvas)
        mIsSaving = false
        return bitmap
    }

    private fun addPath(path: MyPath, options: PaintOptions) {
        mPaths[path] = options
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageBitmap != null) {
            matrix.reset()
            matrix.postTranslate(-imageBitmap!!.width / 2f, -imageBitmap!!.height / 2f)
            matrix.postScale(scaleFactor, scaleFactor)
            matrix.postTranslate(posX + imageBitmap!!.width / 2f, posY + imageBitmap!!.height / 2f)
        }

        imageBitmap?.let {
            canvas.drawBitmap(it, matrix, null)
        }

        for ((key, value) in mPaths) {
            changePaint(value)
            canvas.drawPath(key, mPaint)
        }

        changePaint(mPaintOptions)
        canvas.drawPath(mPath, mPaint)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        mPaint.color = paintOptions.color
        mPaint.style = paintOptions.style
        mPaint.strokeWidth = paintOptions.strokeWidth
    }

    fun clearCanvas() {
        mLastPaths = mPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
        mPath.reset()
        mPaths.clear()
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mCurX = x
        mCurY = y
    }

    private fun actionMove(x: Float, y: Float) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
        mCurX = x
        mCurY = y
    }

    private fun actionUp() {
        mPath.lineTo(mCurX, mCurY)

        // draw a dot on click
        if (mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY)
        }

        mPaths[mPath] = mPaintOptions
        mPath = MyPath()
        mPaintOptions =
            PaintOptions(mPaintOptions.color, mPaintOptions.strokeWidth, mPaintOptions.alpha)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (isInImageMode) {
            adjustImage(event)
            invalidate()
            return true
        }

        when (strokeShape) {
            EShape.CIRCLE -> {}
            EShape.STAR -> {
                drawStar(x, y, mPaintOptions.strokeWidth)
                mUndonePaths.clear()
                invalidate()
                return true
            }

            EShape.HEART -> {
                drawHeart(x, y, mPaintOptions.strokeWidth)
                mUndonePaths.clear()
                invalidate()
                return true
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                actionDown(x, y)
                mUndonePaths.clear()
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
            PaintOptions(mPaintOptions.color, outerRadius, mPaintOptions.alpha, Paint.Style.FILL)
        mPaths[heartPath] = newPaintOptions
    }

    private fun drawStar(centerX: Float, centerY: Float, outerRadius: Float) {
        val starPath = createStarPath(centerX, centerY, outerRadius)
        val newPaintOptions =
            PaintOptions(mPaintOptions.color, 6f, mPaintOptions.alpha)
        mPaths[starPath] = newPaintOptions
    }


    private fun createStarPath(cx: Float, cy: Float, outerRadius: Float): MyPath {
        val section = 2.0 * Math.PI / 5
        val path = MyPath()
        val innerRadius = outerRadius / 3
        val startAngle = -Math.PI / 2 // Start angle set to -90 degrees

        path.reset()
        path.moveTo(
            (cx + outerRadius * cos(startAngle)).toFloat(),
            (cy + outerRadius * sin(startAngle)).toFloat()
        )
        path.lineTo(
            (cx + innerRadius * cos(startAngle + section / 2.0)).toFloat(),
            (cy + innerRadius * sin(startAngle + section / 2.0)).toFloat()
        )

        for (i in 1 until 5) {
            path.lineTo(
                (cx + outerRadius * cos(startAngle + section * i)).toFloat(),
                (cy + outerRadius * sin(startAngle + section * i)).toFloat()
            )
            path.lineTo(
                (cx + innerRadius * cos(startAngle + section * i + section / 2.0)).toFloat(),
                (cy + innerRadius * sin(startAngle + section * i + section / 2.0)).toFloat()
            )
        }

        path.close()
        return path
    }

    private fun createHeartPath(cx: Float, cy: Float, outerRadius: Float): MyPath {
        val path = MyPath()
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