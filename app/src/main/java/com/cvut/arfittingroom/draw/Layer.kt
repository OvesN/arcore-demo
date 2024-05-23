package com.cvut.arfittingroom.draw

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.model.element.impl.Gif
import com.cvut.arfittingroom.draw.path.DrawablePath
import com.cvut.arfittingroom.draw.service.TexturedBrushDrawer
import java.util.LinkedList
import java.util.UUID
import kotlin.collections.HashMap

/**
 * Holds various drawable elements
 *
 * @property id
 * @property width
 * @property height
 *
 * @author Veronika Ovsyannikova
 */
class Layer(
    val id: Int,
    private val width: Int,
    private val height: Int,
) {
    var isVisible: Boolean = true
    val elements = HashMap<UUID, Element>()
    private val elementsToDraw = LinkedList<Element>()
    var curPath = DrawablePath()
    private val curPaint =
        Paint()
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
    var elementToUpdate: Element? = null

    /**
     * Draws the content of the layer
     * The drawing proceeds in the following steps:
     * 1. Draw the elements below the updatable element
     * 2. Draw the element element that should be constantly updated (selected or gif)
     * 3. Draw the elements above the updatable element
     * 4. Draw the current finger painting that the user is creating
     *
     * @param canvas on which to draw the layer's content
     * @param paintOptions for current finger drawing
     */
    fun draw(
        canvas: Canvas,
        paintOptions: PaintOptions,
    ) {
        if (!isVisible) {
            return
        }

        changePaint(paintOptions)

        elementsBelowUpdatableElementBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        elementToUpdate?.draw(canvas)

        elementAboveUpdatableElementBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        if (paintOptions.strokeTextureRef.isNotEmpty()) {
            TexturedBrushDrawer.draw(canvas, curPath, curPaint.strokeWidth)
        } else {
            canvas.drawPath(curPath, curPaint)
        }
    }


    /**
     * Removes an element from the layer by its ID
     *
     * @param elementId The UUID of the element to remove
     */
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

    /**
     * Adds a new element to the layer
     *
     * @param element The element to add
     */
    fun addElement(element: Element) {
        elements[element.id] = element
        elementsToDraw.add(element)

        addToBitmap(element)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        curPaint.color = paintOptions.color
        curPaint.alpha = paintOptions.alpha
        curPaint.strokeWidth = paintOptions.strokeWidth

        if (paintOptions.blurRadius != 0f) {
            curPaint.maskFilter = BlurMaskFilter(paintOptions.blurRadius, paintOptions.blurType)
        } else {
            curPaint.maskFilter = null
        }
    }

    /**
     * Finds the element that intersects with coordinates on this layer
     *
     * @param x The x-coordinate to check for intersection
     * @param y The y-coordinate to check for intersection
     * @return The last drawn element intersecting with the given point,
     * or null if no element intersects
     */
    fun findFirstIntersectedElement(
        x: Float,
        y: Float,
    ): Element? {
        if (!isVisible) {
            return null
        }

        val iterator = elementsToDraw.descendingIterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element.doesIntersect(x, y)) {
                return element
            }
        }

        return null
    }

    /**
     * Deselects all elements in the layer
     */
    fun deselectAllElements() {
        elements.forEach { it.value.setSelected(false) }
    }

    /**
     * Resets all layer's bitmaps
     */
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
    fun createBitmap(): Bitmap? {
        if (!isVisible) {
            return null
        }
        return createBitmapFromElements(elementsToDraw)
    }

    /**
     * Prepare bitmaps
     * Recreate bitmaps for items above and below updatable element
     */
    fun prepareBitmaps() {
        resetBitmaps()

        if (!isVisible) {
            return
        }

        elementsToDraw.forEach {
            if (it != elementToUpdate) {
                it.setSelected(false)
            }
        }

        val selectedElementIndex = elementsToDraw.indexOf(elementToUpdate)

        elementsBelowUpdatableElementBitmap =
            createBitmapFromElements(elementsToDraw.subList(0, selectedElementIndex))

        elementAboveUpdatableElementBitmap =
            if (selectedElementIndex == elementsToDraw.lastIndex) {
                null
            } else {
                createBitmapFromElements(
                    elementsToDraw.subList(
                        selectedElementIndex + 1,
                        elementsToDraw.size,
                    ),
                )
            }
    }

    /**
     * Prepares a bitmap for the entire layer
     */
    fun prepareBitmap() {
        resetBitmaps()
        if (!isVisible) {
            return
        }

        elementsBelowUpdatableElementBitmap = createBitmapFromElements(elementsToDraw)
    }

    private fun createBitmapFromElements(elements: List<Element>): Bitmap? {
        if (!isVisible) {
            return null
        }

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
        if (!isVisible) {
            return
        }

        val bitmap =
            elementAboveUpdatableElementBitmap ?: Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)

        element.draw(canvas)

        elementAboveUpdatableElementBitmap = bitmap
    }

    /**
     * Sets the opacity of the layer
     *
     * @param opacity The new opacity value (0.0 to 1.0)
     */
    fun setOpacity(opacity: Float) {
        this.opacity = opacity
        opacityPaint.apply {
            alpha = (opacity * 255).toInt()
        }
    }

    /**
     * Checks if the layer contains a GIF element
     *
     * @return True if the layer contains a GIF, false otherwise
     */
    fun doesHaveGif(): Boolean = elementsToDraw.firstOrNull { it is Gif }?.let { true } ?: false

    /**
     * Gets the maximum number of frames among all GIF elements in the layer
     *
     * @return The maximum number of frames
     */
    fun getMaxNumberOfFrames() =
        elementsToDraw.filterIsInstance<Gif>().maxOfOrNull { it.gifDrawable?.numberOfFrames ?: 0 }
            ?: 0

    /**
     * Sets all GIF elements in the layer to animation mode
     * It means that on the draw call GIF will draw it's next frame
     */
    fun setAllGifsToAnimationMode() {
        elementsToDraw.forEach {
            if (it is Gif) {
                it.shouldDrawNextFrame = true
                it.currentFrameIndex = 0
                it.increaseFrameIndexEachDraw = true
            }
        }
    }

    /**
     * Resets all GIF elements in the layer to their initial frame
     */
    fun resetAllGifs() {
        elementsToDraw.forEach {
            if (it is Gif) {
                it.currentFrameIndex = 0
            }
        }
    }

    /**
     * Sets all GIF elements in the layer to static mode
     * That means that on the draw call only first frame of the GIF will be drawn
     */
    fun setAllGifsToStaticMode() {
        elementsToDraw.forEach {
            if (it is Gif) {
                it.shouldDrawNextFrame = false
                it.currentFrameIndex = 0
                it.increaseFrameIndexEachDraw = false
            }
        }
    }
}
