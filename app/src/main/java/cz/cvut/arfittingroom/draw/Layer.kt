package cz.cvut.arfittingroom.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.model.PaintOptions
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.element.impl.Gif
import cz.cvut.arfittingroom.draw.path.DrawablePath
import java.util.LinkedList
import java.util.UUID
import kotlin.collections.HashMap


class Layer(
    private val width: Int,
    private val height: Int,
    val id: UUID = UUID.randomUUID()
) {
    var isVisible: Boolean = true
    private val elements = HashMap<UUID, Element>()
    private val elementsToDraw = LinkedList<Element>()
    var curPath = DrawablePath()
    private val curPaint = Paint()
        .apply {
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
    val opacityPaint = Paint()

    /**
     * Range from 0.0 (fully transparent) to 1.0 (fully opaque)
     */
    private var opacity: Float = 1.0f

    private var elementsBelowUpdatableElementBitmap: Bitmap? = null
    private var elementAboveUpdatableElementBitmap: Bitmap? = null

    private var elementToUpdate: Element? = null


    /**
     * Draws the content of the layer
     * The drawing proceeds in the following steps:
     * 1. Draw the elements below the updatable element
     * 2. Draw the element element that should be constantly updated (selected or gif)
     * 3. Draw the elements above the updatable element
     * 4. Draw the current finger painting that the user is creating
     *
     * @param canvas on which to draw the layer's content
     */
    fun draw(canvas: Canvas, paintOptions: PaintOptions) {
        changePaint(paintOptions)

        elementsBelowUpdatableElementBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        elementToUpdate?.draw(canvas)

        elementAboveUpdatableElementBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        canvas.drawPath(curPath, curPaint)
    }

    fun removeElement(elementId: UUID) {
        elements.remove(elementId)
        val iterator = elementsToDraw.iterator()

        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element.id == elementId) {
                element.setSelected(false)
                iterator.remove()
            }
        }

        elementToUpdate?.let {
            if (it.id == elementId) {
                it.setSelected(false)
                elementToUpdate = null
            }
        }

        prepareBitmap()
    }

    fun addElement(element: Element) {
        elements[element.id] = element
        elementsToDraw.add(element)

        addToBitmap(element)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        curPaint.color = paintOptions.color
        curPaint.alpha = (paintOptions.alpha + opacity * 255).toInt().coerceAtMost(255)
        curPaint.style = paintOptions.style
        curPaint.strokeWidth = paintOptions.strokeWidth
    }

    /**
     * Finds the element that intersects with coordinates on this layer
     *
     * @param x The x-coordinate to check for intersection
     * @param y The y-coordinate to check for intersection
     *
     * @return The last drawn element intersecting with the given point,
     * or null if no element intersects
     */
    fun findFirstIntersectedElement(x: Float, y: Float): Element? {
        val iterator = elementsToDraw.descendingIterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element.doIntersect(x, y)) {
                return element
            }
        }

        return null
    }

    fun deselectAllElements() {
        elements.forEach { it.value.setSelected(false) }
    }

    fun resetBitmaps() {
        elementsBelowUpdatableElementBitmap?.recycle()
        elementsBelowUpdatableElementBitmap = null
        elementAboveUpdatableElementBitmap?.recycle()
        elementAboveUpdatableElementBitmap = null
    }

    /**
     * Create bitmap
     *
     * @return bitmap with all elements from this layer
     */
    fun createBitmap(): Bitmap? = createBitmapFromElements(elementsToDraw)

    /**
     * Prepare bitmaps
     * If [selectedElement] is the same as previous one for this layer, do nothing.
     * If not, recreate bitmaps for items above and below this [selectedElement]
     *
     * @param selectedElement
     */
    fun prepareBitmaps(selectedElement: Element) {
        if (selectedElement == this.elementToUpdate) {
            return
        }

        resetBitmaps()

        elementsToDraw.forEach { if (it != selectedElement ) it.setSelected(false) }

        this.elementToUpdate = selectedElement
        val selectedElementIndex = elementsToDraw.indexOf(selectedElement)

        elementsBelowUpdatableElementBitmap =
            createBitmapFromElements(elementsToDraw.subList(0, selectedElementIndex))

        elementAboveUpdatableElementBitmap =
            if (selectedElementIndex == elementsToDraw.lastIndex) null
            else createBitmapFromElements(
                elementsToDraw.subList(
                    selectedElementIndex + 1,
                    elementsToDraw.size
                )
            )
    }

    fun prepareBitmap() {
        resetBitmaps()
        elementsBelowUpdatableElementBitmap = createBitmapFromElements(elementsToDraw)
    }

    private fun createBitmapFromElements(elements: List<Element>): Bitmap? {
        if (elements.isEmpty()) {
            return null
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        elements.forEach {
            it.draw(canvas)
        }

        return bitmap
    }

    private fun addToBitmap(element: Element) {
        val bitmap = elementAboveUpdatableElementBitmap ?: Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        element.draw(canvas)

        elementAboveUpdatableElementBitmap = bitmap
    }

//    private fun createBitmapsWithGif(): List<Bitmap> {
//
//    }

    fun setOpacity(opacity: Float) {
        this.opacity = opacity
        opacityPaint.apply {
            alpha = (opacity * 255).toInt()
        }
    }


    fun doesHaveGif(): Boolean =
        elementsToDraw.firstOrNull{it is Gif}?.let { true } ?: false

}