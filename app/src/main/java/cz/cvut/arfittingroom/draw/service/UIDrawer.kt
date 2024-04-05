package cz.cvut.arfittingroom.draw.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.enums.EElementEditAction
import cz.cvut.arfittingroom.utils.IconUtil
import cz.cvut.arfittingroom.utils.ScreenUtil

class UIDrawer(private val context: Context) {
    private var menuWidth: Float = 0f
    private var menuHeight: Float = 0f
    private var cornerRadius: Float = 0f
    private var textSize: Float = 0f
    private var textPadding: Float = 0f
    private var lineSpacing: Float = 0f
    private var menuItemSpacing: Float = 0f
    private val textPaint: Paint
    private val linePaint: Paint
    private val menuPaint: Paint

    private val editElementIcons: HashMap<EElementEditAction, Bitmap> = hashMapOf()
    private val editElementIconsBounds: HashMap<EElementEditAction, RectF> = hashMapOf()
    private val menuBitmap: Bitmap

    init {
        initializeDimensions()

        menuPaint = Paint().apply {
            color = Color.DKGRAY
            alpha = (255 * 0.9).toInt()
        }

        textPaint = Paint().apply {
            color = Color.WHITE
            textSize = this@UIDrawer.textSize
            textAlign = Paint.Align.LEFT
        }

        linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }

        menuBitmap = prepareMenuBitmap()

        loadEditElementIcons()
    }

    private fun initializeDimensions() {
        menuWidth = ScreenUtil.screenWidth * 0.3f * 1.3f
        menuHeight = ScreenUtil.screenHeight * 0.125f * 1.3f
        cornerRadius = ScreenUtil.screenWidth * 0.02f * 1.3f
        textSize = ScreenUtil.screenHeight * 0.02f * 1.3f
        textPadding = ScreenUtil.screenWidth * 0.025f * 1.3f
        lineSpacing = ScreenUtil.screenHeight * 0.005f * 1.3f
        menuItemSpacing = ScreenUtil.screenHeight * 0.025f * 1.3f
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
                    linePaint
                )
            }
            textY += menuItemSpacing
        }

        return bitmap
    }

    private fun loadEditElementIcons() {
        editElementIcons[EElementEditAction.DELETE] =
            IconUtil.changeIconColor(
                BitmapFactory.decodeResource(context.resources, R.drawable.delete_icon),
                Color.YELLOW
            )
        editElementIcons[EElementEditAction.ROTATE] =
            IconUtil.changeIconColor(
                BitmapFactory.decodeResource(context.resources, R.drawable.rotate_icon),
                Color.YELLOW
            )
        editElementIcons[EElementEditAction.SCALE] =
            IconUtil.changeIconColor(
                (BitmapFactory.decodeResource(context.resources, R.drawable.scale_icon)),
                Color.YELLOW
            )
        editElementIcons[EElementEditAction.MENU] =
            IconUtil.changeIconColor(
                (BitmapFactory.decodeResource(context.resources, R.drawable.menu_icon)),
                Color.YELLOW
            )
    }

    fun drawSelectedElementEditIcons(
        canvas: Canvas,
        selectedElement: Element?,
        isInElementMenuMode: Boolean
    ) {
        selectedElement?.let { element ->
            // Do not draw anything if element was deselected
            // previously (for example in undo/redo action)
            if (!element.isSelected) {
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
                canvas.drawBitmap(
                    menuBitmap,
                    boundingBox.topRightCornerCoor.x - menuBitmap.width,
                    boundingBox.topRightCornerCoor.y, null
                )

                // Initial menu item Y position
                var itemY = boundingBox.topRightCornerCoor.y + textPadding

                val menuX = boundingBox.topRightCornerCoor.x - menuBitmap.width


                // Defining RectF for each menu item
                editElementIconsBounds[EElementEditAction.MOVE_UP] =
                    RectF(menuX, itemY, menuX + menuWidth, itemY + textSize)

                //canvas.drawRect(RectF(menuX, itemY, menuX + menuWidth, itemY + textSize), Paint().apply { color = Color.BLUE })
                itemY += textSize + lineSpacing  // Increment Y position for the next item

                editElementIconsBounds[EElementEditAction.MOVE_DOWN] =
                    RectF(menuX, itemY, menuX + menuWidth, itemY + textSize)
                // canvas.drawRect(RectF(menuX, itemY, menuX + menuWidth, itemY + textSize), Paint().apply { color = Color.BLACK })

                itemY += textSize + lineSpacing

                editElementIconsBounds[EElementEditAction.MOVE_TO] =
                    RectF(menuX, itemY, menuX + menuWidth, itemY + textSize)

                //canvas.drawRect(RectF(menuX, itemY, menuX + menuWidth, itemY + textSize), Paint().apply { color = Color.YELLOW })
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

    fun checkEditButtons(x: Float, y: Float): EElementEditAction? =
        editElementIconsBounds.entries.firstOrNull { it.value.contains(x, y) }?.key
}
