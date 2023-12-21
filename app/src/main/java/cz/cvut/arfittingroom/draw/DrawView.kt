package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import com.divyanshu.draw.widget.MyPath
import com.divyanshu.draw.widget.PaintOptions
import mu.KotlinLogging
import java.util.LinkedHashMap
import kotlin.math.cos
import kotlin.math.sin

//TODO slightly modified android draw
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
            changePaint(value)
            canvas.drawPath(key, mPaint)
        }

        changePaint(mPaintOptions)
        canvas.drawPath(mPath, mPaint)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        mPaint.color = paintOptions.color
        //TODO temporal
        if (strokeShape == EShape.STAR) {
            mPaint.strokeWidth = 6f
        }
        else {
            mPaint.strokeWidth = paintOptions.strokeWidth
        }
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

    private fun drawStar(centerX: Float, centerY: Float, outerRadius: Float) {
        val starPath = createStarPath(centerX, centerY, outerRadius)
        val newPaintOptions =
            PaintOptions(mPaintOptions.color, outerRadius, mPaintOptions.alpha)
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

}