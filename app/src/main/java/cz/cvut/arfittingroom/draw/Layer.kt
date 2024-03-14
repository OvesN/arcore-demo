package cz.cvut.arfittingroom.draw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.LinkedList
import java.util.UUID
import kotlin.collections.HashMap

class Layer(private val width: Int, private val height: Int) {
    val id: UUID = UUID.randomUUID()
    private var bitmap: Bitmap
    var isVisible: Boolean = true
    private var opacity: Float = 1.0f // Range from 0.0 (fully transparent) to 1.0 (fully opaque)
    val elements = HashMap<UUID, Element>() // Map of elements on the layer, key is element id
    val actions = HashMap<UUID, LinkedList<Command<out Element>>>() // Map of actions to do on this layer, keys is element id

    init {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
        actions.remove(elementId)
    }

    fun addElement(element: Element, relatedActions: List<Command<out Element>>) {
        elements[element.id] = element
        actions[element.id] = LinkedList(relatedActions)
    }

    private fun createBitmap() {
        resetBitmap()

        val canvas = Canvas(bitmap)

        actions.values.forEach { elementActions -> elementActions
            .forEach { it.execute(canvas) } }
    }

    private fun resetBitmap() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}