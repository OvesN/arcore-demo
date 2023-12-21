package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
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

        for ((key, value) in mPaths) {
            //TODO VERY STUPID
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

        path.reset()
        path.moveTo(
            (cx + outerRadius * cos(0.0)).toFloat(),
            (cy + outerRadius * sin(0.0)).toFloat()
        )
        path.lineTo(
            (cx + innerRadius * cos(section / 2.0)).toFloat(),
            (cy + innerRadius * sin(section / 2.0)).toFloat()
        )

        for (i in 1 until 5) {
            path.lineTo(
                (cx + outerRadius * cos(section * i)).toFloat(),
                (cy + outerRadius * sin(section * i)).toFloat()
            )
            path.lineTo(
                (cx + innerRadius * cos(section * i + section / 2.0)).toFloat(),
                (cy + innerRadius * sin(section * i + section / 2.0)).toFloat()
            )
        }

        path.close()
        return path
    }

    private fun createHeartPath(cx: Float, cy: Float, outerRadius: Float): MyPath {
        val path = MyPath()
        val startX = outerRadius / 2 + cx
        val startY = outerRadius / 5 + cy
        // Starting point
        path.moveTo(startX, startY)

        // Upper left path
        path.cubicTo(
            5 * outerRadius / 14 + startX, startY,
            startX, outerRadius / 15 + startY,
            outerRadius / 28 + startX, 2 * outerRadius / 5 + startY
        )

        // Lower left path
        path.cubicTo(
            outerRadius / 14 + startX, 2 * outerRadius / 3 + startY,
            3 * outerRadius / 7 + startX, 5 * outerRadius / 6 + startY,
            outerRadius / 2 + startX, outerRadius + startY
        )

        // Lower right path
        path.cubicTo(
            4 * outerRadius / 7 + startX, 5 * outerRadius / 6 + startY,
            13 * outerRadius / 14 + startX, 2 * outerRadius / 3 + startY,
            27 * outerRadius / 28 + startX, 2 * outerRadius / 5 + startY
        )

        // Upper right path
        path.cubicTo(
            outerRadius + startX, outerRadius / 15 + startY,
            9 * outerRadius / 14 + startX, 0f + startY,
            outerRadius / 2 + startX, outerRadius / 5 + startY
        )

        return path
    }

    private fun createHeartPath1(cx: Float, cy: Float, outerRadius: Float): MyPath {
        val path = MyPath()

        // Adjust the starting point
        val startX = cx - outerRadius / 2
        val startY = cy - outerRadius / 5
        path.moveTo(startX, startY)

        // Upper left curve
        path.cubicTo(
            cx - 4 * outerRadius / 14, cy - 5 * outerRadius / 5,
            cx - 9 * outerRadius / 14, cy - 14 * outerRadius / 15,
            cx - 27 * outerRadius / 28, cy - 3 * outerRadius / 5
        )

        // Lower left curve
        path.cubicTo(
            cx - 13 * outerRadius / 14, cy + outerRadius / 3,
            cx - 3 * outerRadius / 7, cy + 5 * outerRadius / 6,
            cx, cy + outerRadius
        )

        // Lower right curve
        path.cubicTo(
            cx + 3 * outerRadius / 7, cy + 5 * outerRadius / 6,
            cx + 13 * outerRadius / 14, cy + outerRadius / 3,
            cx + 27 * outerRadius / 28, cy - 3 * outerRadius / 5
        )

        // Upper right curve
        path.cubicTo(
            cx + 9 * outerRadius / 14, cy - 14 * outerRadius / 15,
            cx + 4 * outerRadius / 14, cy - 5 * outerRadius / 5,
            cx, startY
        )

        path.close()

        return path
    }


}