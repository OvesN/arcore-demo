package com.cvut.arfittingroom.draw.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.get
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.model.enums.EElementEditAction
import java.lang.IllegalArgumentException
import kotlin.math.sqrt

class UIDrawer(private val context: Context) {
    private var menuWidth: Float = 0f
    private var menuHeight: Float = 0f
    private var cornerRadius: Float = 0f
    private var textSize: Float = 0f
    private var textPadding: Float = 0f
    private var lineSpacing: Float = 0f
    private var menuItemSpacing: Float = 0f
    private val textPaint: Paint =
        Paint().apply {
            color = Color.WHITE
            textSize = this@UIDrawer.textSize
            textAlign = Paint.Align.LEFT
        }
    private val linePaint: Paint =
        Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }
    private val menuPaint: Paint =
        Paint().apply {
            color = Color.DKGRAY
            alpha = (255 * 0.9).toInt()
        }
    private val editElementIcons: HashMap<EElementEditAction, Bitmap> = hashMapOf()
    private val editElementIconsBounds: HashMap<EElementEditAction, RectF> = hashMapOf()
    private var menuBitmap: Bitmap? = null
    private var faceTextureBitmap: Bitmap? = null
    private var backgroundBitmap: Bitmap? = null
    private var backgroundBitmapMatrix = Matrix()
    private var faceTextureMatrix: Matrix = Matrix()
    private var faceTextureVector: VectorDrawableCompat? = null
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    private fun prepareMatrix(bitmap: Bitmap?): Matrix {
        val bitmapWidth =
            bitmap?.width ?: faceTextureVector?.intrinsicWidth ?: 0
        val bitmapHeight =
            bitmap?.height ?: faceTextureVector?.intrinsicHeight
            ?: 0

        val scale =
            (viewWidth.toFloat() / bitmapWidth).coerceAtMost(viewHeight.toFloat() / bitmapHeight)

        val x = (viewWidth - bitmapWidth * scale) / 2
        val y = (viewHeight - bitmapHeight * scale) / 2

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(x, y)

        return matrix
    }

    fun setDimensions(
        width: Int,
        height: Int,
    ) {
        viewWidth = width
        viewHeight = height
        initializeDimensions()

        menuBitmap = prepareMenuBitmap()
        faceTextureVector =
            VectorDrawableCompat.create(context.resources, R.drawable.facemesh, null)
        faceTextureBitmap = faceTextureVector?.toBitmap()
        faceTextureMatrix = prepareMatrix(faceTextureBitmap)
        loadEditElementIcons()
    }

    private fun initializeDimensions() {
        menuWidth = viewWidth * 0.3f * 1.3f
        menuHeight = viewHeight * 0.125f * 1.3f
        cornerRadius = viewWidth * 0.02f * 1.3f
        textSize = viewHeight * 0.02f * 1.3f
        textPadding = viewWidth * 0.025f * 1.3f
        lineSpacing = viewHeight * 0.005f * 1.3f
        menuItemSpacing = viewHeight * 0.025f * 1.3f
    }

    private fun prepareMenuBitmap(): Bitmap {
        val menuX = 0f
        val menuY = 0f

        // Draw the background of the menu
        val rect = RectF(menuX, menuY, menuX + menuWidth, menuY + menuHeight)
        val bitmap =
            Bitmap.createBitmap(menuWidth.toInt(), menuHeight.toInt(), Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, menuPaint)

        // Draw menu items
        val menuItems = listOf("Move up", "Move down", "Move to", "Change color")
        var textY = menuY + textPadding + textSize
        for ((index, item) in menuItems.withIndex()) {
            // Draw text
            canvas.drawText(item, menuX + textPadding, textY, textPaint)

            // Draw line if not the last item
            if (index < menuItems.size - 1) {
                canvas.drawLine(
                    menuX + textPadding,
                    textY + lineSpacing,
                    menuX + menuWidth - textPadding,
                    textY + lineSpacing,
                    linePaint,
                )
            }
            textY += menuItemSpacing
        }

        return bitmap
    }

    private fun loadEditElementIcons() {
        editElementIcons[EElementEditAction.DELETE] =
            VectorDrawableCompat.create(context.resources, R.drawable.yellow_delete, null)
                ?.toBitmap()!!

        editElementIcons[EElementEditAction.ROTATE] =
            VectorDrawableCompat.create(context.resources, R.drawable.rotate, null)?.toBitmap()!!

        editElementIcons[EElementEditAction.SCALE] =
            VectorDrawableCompat.create(context.resources, R.drawable.rezise, null)?.toBitmap()!!

        editElementIcons[EElementEditAction.MENU] =
            VectorDrawableCompat.create(context.resources, R.drawable.yellow_menu, null)
                ?.toBitmap()!!
    }

    fun drawFaceTextureImage(canvas: Canvas) {
        faceTextureBitmap?.let { canvas.drawBitmap(it, faceTextureMatrix, null) }
    }

    fun drawSelectedElementEditIcons(
        canvas: Canvas,
        selectedElement: Element?,
        isInElementMenuMode: Boolean,
    ) {
        selectedElement?.let { element ->
            // Do not draw anything if element was deselected
            // previously (for example in undo/redo action)
            if (!element.isSelected()) {
                return
            }

            val boundingBox = element.boundingBox

            // Menu Icon (Top Right Corner)
            editElementIcons[EElementEditAction.MENU]?.let { icon ->
                val x = boundingBox.topRightCornerCoor.x
                val y = boundingBox.topRightCornerCoor.y - icon.height
                canvas.drawBitmap(icon, x, y, null)
                editElementIconsBounds[EElementEditAction.MENU] =
                    RectF(x, y, x + icon.width, y + icon.height)
            }

            // Scale Icon (Bottom Right Corner)
            editElementIcons[EElementEditAction.SCALE]?.let { icon ->
                val x = boundingBox.bottomRightCornerCoor.x
                val y = boundingBox.bottomRightCornerCoor.y
                canvas.drawBitmap(icon, x, y, null)
                editElementIconsBounds[EElementEditAction.SCALE] =
                    RectF(x, y, x + icon.width, y + icon.height)
            }

            // Rotate Icon (Bottom Left Corner)
            editElementIcons[EElementEditAction.ROTATE]?.let { icon ->
                val x = boundingBox.bottomLeftCornerCoor.x - icon.width
                val y = boundingBox.bottomLeftCornerCoor.y
                canvas.drawBitmap(icon, x, y, null)
                editElementIconsBounds[EElementEditAction.ROTATE] =
                    RectF(x, y, x + icon.width, y + icon.height)
            }

            // Delete Icon (Top Left Corner)
            editElementIcons[EElementEditAction.DELETE]?.let { icon ->
                val x = boundingBox.topLeftCornerCoor.x - icon.width
                val y = boundingBox.topLeftCornerCoor.y - icon.height
                canvas.drawBitmap(icon, x, y, null)
                editElementIconsBounds[EElementEditAction.DELETE] =
                    RectF(x, y, x + icon.width, y + icon.height)
            }

            if (isInElementMenuMode) {
                if (menuBitmap == null) {
                    return
                }
                menuBitmap?.let {
                    canvas.drawBitmap(
                        it,
                        boundingBox.topRightCornerCoor.x - menuBitmap!!.width,
                        boundingBox.topRightCornerCoor.y,
                        null,
                    )
                }

                // Initial menu item Y position
                var itemY = boundingBox.topRightCornerCoor.y + textPadding

                val menuX = boundingBox.topRightCornerCoor.x - menuBitmap!!.width

                // Defining RectF for each menu item
                editElementIconsBounds[EElementEditAction.MOVE_UP] =
                    RectF(menuX, itemY, menuX + menuWidth, itemY + textSize)

                // canvas.drawRect(RectF(menuX, itemY, menuX + menuWidth, itemY + textSize), Paint().apply { color = Color.BLUE })
                itemY += textSize + lineSpacing  // Increment Y position for the next item

                editElementIconsBounds[EElementEditAction.MOVE_DOWN] =
                    RectF(menuX, itemY, menuX + menuWidth, itemY + textSize)
                // canvas.drawRect(RectF(menuX, itemY, menuX + menuWidth, itemY + textSize), Paint().apply { color = Color.BLACK })

                itemY += textSize + lineSpacing

                editElementIconsBounds[EElementEditAction.MOVE_TO] =
                    RectF(menuX, itemY, menuX + menuWidth, itemY + textSize)

                // canvas.drawRect(RectF(menuX, itemY, menuX + menuWidth, itemY + textSize), Paint().apply { color = Color.YELLOW })
                itemY += textSize + lineSpacing

                editElementIconsBounds[EElementEditAction.CHANGE_COLOR] =
                    RectF(menuX, itemY, menuX + menuWidth, itemY + textSize)

                // canvas.drawRect(RectF(menuX, itemY, menuX + menuWidth, itemY + textSize), Paint().apply { color = Color.RED })
            } else {
                editElementIconsBounds.remove(EElementEditAction.MOVE_UP)
                editElementIconsBounds.remove(EElementEditAction.MOVE_DOWN)
                editElementIconsBounds.remove(EElementEditAction.MOVE_TO)
                editElementIconsBounds.remove(EElementEditAction.CHANGE_COLOR)
            }
        }
    }

    fun drawPipette(
        canvas: Canvas,
        canvasTransformationMatrix: Matrix,
        lastTouchX: Float, lastTouchY: Float,
        bitmap: Bitmap?
    ): Int {
        val points = floatArrayOf(lastTouchX, lastTouchY)
        canvasTransformationMatrix.mapPoints(points)

        val radius = 100f
        val transformedRadius = mapRadius(canvasTransformationMatrix, radius)
        val strokeExtra = 10f
        val transformedStrokeWidth = mapRadius(canvasTransformationMatrix, strokeExtra)
        val verticalOffset =
            mapRadius(canvasTransformationMatrix, 200f)
        val selectedColor =
            bitmap?.let {
                try {
                    it[lastTouchX.toInt(), (lastTouchY - verticalOffset).toInt()]
                } catch (ex: IllegalArgumentException) {
                    Color.TRANSPARENT
                }
            }
                ?: Color.TRANSPARENT

        val pipettePaint = Paint().apply {
            style = Paint.Style.FILL
            color = selectedColor
        }

        val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = transformedStrokeWidth
        }

        val crossCenterY = lastTouchY - verticalOffset

        canvas.drawCircle(lastTouchX, crossCenterY, transformedRadius + strokeExtra, strokePaint)
        canvas.drawCircle(
            lastTouchX, crossCenterY, transformedRadius, pipettePaint
        )
        val crossLength = 20f
        val crossPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 5f
        }

        canvas.drawLine(
            lastTouchX - crossLength,
            crossCenterY,
            lastTouchX + crossLength,
            crossCenterY,
            crossPaint
        )

        // Vertical line of the cross
        canvas.drawLine(
            lastTouchX,
            crossCenterY - crossLength,
            lastTouchX,
            crossCenterY + crossLength,
            crossPaint
        )

        return selectedColor
    }

    private fun mapRadius(matrix: Matrix, radius: Float): Float {
        val inverse = Matrix()
        matrix.invert(inverse)
        val vector = floatArrayOf(radius, 0f)
        inverse.mapVectors(vector) // Transform for scale and rotation without translation
        return sqrt(vector[0] * vector[0] + vector[1] * vector[1])
    }

    fun setBackgroundBitmap(bitmap: Bitmap?) {
        backgroundBitmap = bitmap
        backgroundBitmapMatrix = prepareMatrix(bitmap)
    }

    fun checkEditButtons(
        x: Float,
        y: Float,
    ): EElementEditAction? =
        editElementIconsBounds.entries.firstOrNull { it.value.contains(x, y) }?.key

    fun drawBackground(canvas: Canvas) {
        backgroundBitmap?.let { canvas.drawBitmap(it, backgroundBitmapMatrix, null) }
    }
}
