package cz.cvut.arfittingroom.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import java.util.LinkedList
import java.util.UUID
import kotlin.collections.HashMap

class Layer(private val width: Int, private val height: Int) {
    val id: UUID = UUID.randomUUID()
    var isVisible: Boolean = true
    val elements = HashMap<UUID, Element>() // Map of elements on the layer, key is element id
    val elementsToDraw = LinkedList<Element>() // Map of actions to do on this layer, keys is element id

    private var curDrawingPath = LinkedHashMap<DrawablePath, PaintOptions>()
    var curPath = DrawablePath()

    private val curPaint = Paint()
    private var opacity: Float = 1.0f // Range from 0.0 (fully transparent) to 1.0 (fully opaque)
    private var bitmap: Bitmap

    init {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        curPaint.apply {
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
    }

    fun draw(canvas: Canvas) {
        createBitmap()

        // Draw the bitmap for layer
        if (isVisible) {
            val paint = Paint().apply {
                alpha = (opacity * 255).toInt()
            }
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        }
    }

    fun removeElement(elementId: UUID) {
        elements.remove(elementId)
        val iterator = elementsToDraw.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element.id == elementId) {
                element.isSelected = false
                iterator.remove()
            }
        }
    }

    fun addElement(element: Element) {
        elements[element.id] = element
        elementsToDraw.add(element)
    }

    private fun createBitmap() {
        resetBitmap()

        val canvas = Canvas(bitmap)

        elementsToDraw.forEach { it.draw(canvas) }

        // Draw the current part of the part that the user is drawing
        canvas.drawPath(curPath, curPaint)

        // Draw the current path that the user is drawing
        curDrawingPath.forEach {
            changePaint(it.value)
            canvas.drawPath(it.key, curPaint)
        }
    }

    private fun resetBitmap() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun changePaint(paintOptions: PaintOptions) {
        curPaint.color = paintOptions.color
        curPaint.alpha = paintOptions.alpha
        curPaint.style = paintOptions.style
        curPaint.strokeWidth = paintOptions.strokeWidth
    }

    fun findFirstIntersectedElement(x: Float, y: Float): Element? {
        val iterator = elementsToDraw.descendingIterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element.doIntersect(x, y)) {
                return element
            }
        }
        // No intersecting element was found
        return null
    }

    fun deselectAllElements() {
        elements.forEach{it.value.isSelected = false}
    }
}