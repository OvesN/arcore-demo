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

const val ELEMENT_MENU_ICON_SIZE = 70f
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
            color = Color.GRAY
            alpha = (255 * 0.9).toInt()
        }
    private val editElementIcons: HashMap<EElementEditAction, Bitmap> = hashMapOf()
    private val editElementIconsBounds: HashMap<EElementEditAction, RectF> = hashMapOf()
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
        canvasScaleFactor: Float
    ) {
        selectedElement?.let { element ->
            // Do not draw anything if element was deselected
            // previously (for example in undo/redo action)
            if (!element.isSelected()) {
                return
            }

            val boundingBox = element.boundingBox
            val scaledIconSize = ELEMENT_MENU_ICON_SIZE / canvasScaleFactor

            drawEditIcon(canvas, EElementEditAction.MENU, boundingBox.topRightCornerCoor.x, boundingBox.topRightCornerCoor.y - scaledIconSize, scaledIconSize)
            drawEditIcon(canvas, EElementEditAction.SCALE, boundingBox.bottomRightCornerCoor.x, boundingBox.bottomRightCornerCoor.y, scaledIconSize)
            drawEditIcon(canvas, EElementEditAction.ROTATE, boundingBox.bottomLeftCornerCoor.x - scaledIconSize, boundingBox.bottomLeftCornerCoor.y, scaledIconSize)
            drawEditIcon(canvas, EElementEditAction.DELETE, boundingBox.topLeftCornerCoor.x - scaledIconSize, boundingBox.topLeftCornerCoor.y - scaledIconSize, scaledIconSize)

            if (isInElementMenuMode) {
                drawMenu(canvas, boundingBox.topRightCornerCoor.x, boundingBox.topRightCornerCoor.y, canvasScaleFactor)
            } else {
                removeMenuBounds()
            }
        }
    }

    private fun drawEditIcon(canvas: Canvas, action: EElementEditAction, x: Float, y: Float, scaledIconSize: Float) {
        editElementIcons[action]?.let { icon ->
            val scaledIcon = Bitmap.createScaledBitmap(icon, scaledIconSize.toInt(), scaledIconSize.toInt(), true)
            canvas.drawBitmap(scaledIcon, x, y, null)
            editElementIconsBounds[action] = RectF(x, y, x + scaledIconSize, y + scaledIconSize)
        }
    }

    private fun drawMenu(canvas: Canvas, menuX: Float, menuY: Float, canvasScaleFactor: Float) {
        val menuBitmap = createMenuBitmap(canvasScaleFactor)
        canvas.drawBitmap(menuBitmap, menuX - menuBitmap.width, menuY, null)

        // Initial menu item Y position
        var itemY = menuY + textPadding / canvasScaleFactor

        val adjustedMenuX = menuX - menuBitmap.width

        // Define RectF for each menu item
        defineMenuBounds(EElementEditAction.LAYER_UP, adjustedMenuX, itemY, canvasScaleFactor)
        itemY += textSize / canvasScaleFactor + lineSpacing / canvasScaleFactor

        defineMenuBounds(EElementEditAction.LAYER_DOWN, adjustedMenuX, itemY, canvasScaleFactor)
        itemY += textSize / canvasScaleFactor + lineSpacing / canvasScaleFactor

        defineMenuBounds(EElementEditAction.TO_LAYER, adjustedMenuX, itemY, canvasScaleFactor)
        itemY += textSize / canvasScaleFactor + lineSpacing / canvasScaleFactor

        defineMenuBounds(EElementEditAction.CHANGE_COLOR, adjustedMenuX, itemY, canvasScaleFactor)
    }

    private fun defineMenuBounds(action: EElementEditAction, x: Float, y: Float, canvasScaleFactor: Float) {
        editElementIconsBounds[action] = RectF(x, y, x + menuWidth / canvasScaleFactor, y + textSize / canvasScaleFactor)
    }

    private fun removeMenuBounds() {
        editElementIconsBounds.remove(EElementEditAction.LAYER_UP)
        editElementIconsBounds.remove(EElementEditAction.LAYER_DOWN)
        editElementIconsBounds.remove(EElementEditAction.TO_LAYER)
        editElementIconsBounds.remove(EElementEditAction.CHANGE_COLOR)
    }

    private fun createMenuBitmap(canvasScaleFactor: Float): Bitmap {
        val menuX = 0f
        val menuY = 0f
        val scaledMenuWidth = menuWidth / canvasScaleFactor
        val scaledMenuHeight = menuHeight / canvasScaleFactor

        val rect = RectF(menuX, menuY, menuX + scaledMenuWidth, menuY + scaledMenuHeight)
        val bitmap = Bitmap.createBitmap(scaledMenuWidth.toInt(), scaledMenuHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Scale menu paint properties
        val scaledCornerRadius = cornerRadius / canvasScaleFactor
        val scaledTextSize = textSize / canvasScaleFactor
        val scaledTextPadding = textPadding / canvasScaleFactor
        val scaledLineSpacing = lineSpacing / canvasScaleFactor
        val scaledMenuItemSpacing = menuItemSpacing / canvasScaleFactor

        textPaint.textSize = scaledTextSize

        canvas.drawRoundRect(rect, scaledCornerRadius, scaledCornerRadius, menuPaint)

        // Draw menu items
        val menuItems = listOf(
            EElementEditAction.LAYER_UP.actionName,
            EElementEditAction.LAYER_DOWN.actionName,
            EElementEditAction.TO_LAYER.actionName,
            EElementEditAction.CHANGE_COLOR.actionName
        )

        var textY = menuY + scaledTextPadding + scaledTextSize
        for ((index, item) in menuItems.withIndex()) {
            canvas.drawText(item, menuX + scaledTextPadding, textY, textPaint)

            if (index < menuItems.size - 1) {
                canvas.drawLine(
                    menuX + scaledTextPadding,
                    textY + scaledLineSpacing,
                    menuX + scaledMenuWidth - scaledTextPadding,
                    textY + scaledLineSpacing,
                    linePaint,
                )
            }
            textY += scaledMenuItemSpacing
        }

        return bitmap
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

        canvas.drawCircle(
            lastTouchX,
            crossCenterY,
            transformedRadius + strokeExtra,
            strokePaint
        )
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
