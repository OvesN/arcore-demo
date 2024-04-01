package cz.cvut.arfittingroom.draw

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import com.chillingvan.canvasgl.CanvasGL
import com.chillingvan.canvasgl.ICanvasGL
import com.chillingvan.canvasgl.glcanvas.GLCanvas
import com.chillingvan.canvasgl.glcanvas.GLPaint
import com.chillingvan.canvasgl.glcanvas.RawTexture
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter
import cz.cvut.arfittingroom.draw.model.PaintOptions
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.utils.drawPath
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue

class Layer(private val width: Int, private val height: Int) {
    val id: UUID = UUID.randomUUID()
    var isVisible: Boolean = true
    val elements =
        ConcurrentHashMap<UUID, Element>() // Map of elements on the layer, key is element id
    val elementsToDraw =
        ConcurrentLinkedQueue<Element>() // Map of actions to do on this layer, keys is element id

    private var curDrawingPath = LinkedHashMap<DrawablePath, PaintOptions>()
    var curPath = DrawablePath()

    private val curPaint = GLPaint()
    private var opacity: Float = 1.0f // Range from 0.0 (fully transparent) to 1.0 (fully opaque)

    private var texture: RawTexture = RawTexture(width, height, false)


    init {
        curPaint.apply {
            //strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
    }

    fun draw(canvas: ICanvasGL) {
        drawTexture(canvas)
        // Draw the bitmap for layer
        if (isVisible) {
//            val paint = Paint().apply {
//                alpha = (opacity * 255).toInt()
//            }
            canvas.glCanvas.drawTexture(
                texture,
                0,
                0,
                texture.width,
                texture.height,
                BasicTextureFilter(),
                null
            )
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

    private fun drawTexture(glCanvas: ICanvasGL) {

        glCanvas.beginRenderTarget(texture)

        glCanvas.clearBuffer(Color.TRANSPARENT)

        elementsToDraw.forEach { it.draw(glCanvas) }

        // Draw the current part of the part that the user is drawing
        glCanvas.drawPath(curPath, curPaint)

        // Draw the current path that the user is drawing

        curDrawingPath.forEach {
            changePaint(it.value)
            glCanvas.drawPath(it.key, curPaint)
        }


        glCanvas.endRenderTarget()
    }

    fun changePaint(paintOptions: PaintOptions) {
        curPaint.color = paintOptions.color
        //curPaint.alpha = paintOptions.alpha
        curPaint.style = paintOptions.style
        curPaint.lineWidth = paintOptions.strokeWidth
    }

    fun findFirstIntersectedElement(x: Float, y: Float): Element? =
        elementsToDraw.findLast { it.doIntersect(x, y) }


    fun deselectAllElements() {
        elements.forEach { it.value.isSelected = false }
    }

}