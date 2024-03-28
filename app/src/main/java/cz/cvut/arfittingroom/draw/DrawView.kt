package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.globalHistory
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.command.action.AddElementToLayer
import cz.cvut.arfittingroom.draw.command.action.ScaleElement
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.element.impl.Curve
import cz.cvut.arfittingroom.draw.model.element.impl.Heart
import cz.cvut.arfittingroom.draw.model.element.impl.Star
import cz.cvut.arfittingroom.draw.model.enums.EShape
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.service.LayerManager
import mu.KotlinLogging
import javax.inject.Inject
import kotlin.math.abs


private val logger = KotlinLogging.logger { }

private const val SPAN_SLOP = 7

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    @Inject
    lateinit var layerManager: LayerManager
    private var isLayerInitialized = false
    private var paintOptions = PaintOptions()

    private var curX = 0f
    private var curY = 0f
    private var startX = 0f
    private var startY = 0f
    private var isSaving = false
    private var isStrokeWidthBarEnabled = false

    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    private var imageBitmap: Bitmap? = null
    var selectedElement: Element? = null

    var isInImageMode = false
    var strokeShape = EShape.CIRCLE

    private var ignoreNextOneFingerMove = false

    init {
        (context.applicationContext as? ARFittingRoomApplication)?.appComponent?.inject(this)

        // Add event for element scaling
        scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    ignoreNextOneFingerMove = true
                    return super.onScaleBegin(detector)
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    if (gestureTolerance(detector)) {
                        scaleFactor *= detector.scaleFactor
                        scaleFactor = 0.1f.coerceAtLeast(scaleFactor.coerceAtMost(6.0f))

                        (selectedElement as? Scalable)?.continuousScale(scaleFactor)
                        invalidate()
                        return true
                    }
                    return false
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    selectedElement?.endContinuousScale()
                    selectedElement?.let {
                        it.endContinuousScale()
                        DrawHistoryHolder.addToHistory(ScaleElement(it, scaleFactor))
                    }

                    ignoreNextOneFingerMove = true
                    scaleFactor = 1f

                    super.onScaleEnd(detector)
                }

                private fun gestureTolerance(detector: ScaleGestureDetector): Boolean {
                    val spanDelta = abs(detector.currentSpan - detector.previousSpan)
                    return spanDelta > SPAN_SLOP
                }
            })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        // Handle multi-touch events for scaling
        //TODO HAndle deselecting while scaling
        if (event.pointerCount == 2 && selectedElement != null) {
            scaleGestureDetector.onTouchEvent(event)
            return true
        }

        //TODO fix this, different modes and different brushes?
        else when (strokeShape) {
            EShape.NONE -> handleElementDeformationMode(event, x, y)
            EShape.CIRCLE -> handleDrawingMode(event, x, y)
            else -> handleStampMode(event, x, y)
        }

        invalidate()
        return true
    }

    private fun handleElementDeformationMode(event: MotionEvent, x: Float, y: Float) {
        if (!scaleGestureDetector.isInProgress) {

            when (event.action) {
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                    // When the last finger is lifted, reset ignoreNextMove.
                    if (event.pointerCount - 1 == 0) { // Subtract 1 because the count includes the finger that is being lifted.
                        ignoreNextOneFingerMove = false
                    }
                }

                MotionEvent.ACTION_DOWN -> selectedElement = layerManager.selectElement(x, y)
                MotionEvent.ACTION_MOVE -> if (!ignoreNextOneFingerMove) {
                    selectedElement?.move(x, y)
                }
            }
        }
    }

    private fun handleDrawingMode(event: MotionEvent, x: Float, y: Float) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                actionDown(x, y)
            }

            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }
    }

    private fun handleStampMode(event: MotionEvent, x: Float, y: Float) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (strokeShape) {
                EShape.STAR -> drawStar(x, y, paintOptions.strokeWidth)
                EShape.HEART -> drawHeart(x, y, paintOptions.strokeWidth)
                else -> {}
            }
        }
    }

    fun undo() {
        DrawHistoryHolder.undo()
        invalidate()
    }

    fun redo() {
        DrawHistoryHolder.redo()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //Initialize first layer
        if (!isLayerInitialized) {
            layerManager.addLayer(w, h)
            isLayerInitialized = true
        }
    }

    private fun adjustImage(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scaleGestureDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    posX += dx
                    posY += dy
                    invalidate()
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
        }
        return true
    }

    fun addLayer(): Int {
        val newLayerIndex = layerManager.addLayer(width, height)
        layerManager.activeLayerIndex = newLayerIndex
        return newLayerIndex
    }

    //Return true if new layer is selected
    fun setActiveLayer(layerIndex: Int): Boolean {
        if (layerIndex >= layerManager.getNumOfLayers() || layerIndex < 0) {
            return false
        }
        layerManager.activeLayerIndex = layerIndex
        logger.info { "Active layer is now $layerIndex" }

        return true
    }

    fun setColor(newColor: Int) {
        @ColorInt
        paintOptions.color = newColor
        paintOptions.alpha = newColor.alpha
        if (isStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        paintOptions.strokeWidth = newStrokeWidth
        if (isStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun getBitmap(): Bitmap {
        layerManager.deselectAllElements()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        isSaving = true
        draw(canvas)
        isSaving = false

        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw all layers
        layerManager.drawLayers(canvas, paintOptions)
    }

    fun clearCanvas() {
        layerManager.deleteLayers()
        globalHistory.clear()
        // Add initial layer
        layerManager.addLayer(width, height)
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        layerManager.getCurPath().reset()
        layerManager.getCurPath().moveTo(x, y)
        curX = x
        curY = y
    }

    private fun actionMove(x: Float, y: Float) {
        layerManager.getCurPath().quadTo(curX, curY, (x + curX) / 2, (y + curY) / 2)
        curX = x
        curY = y
    }

    private fun actionUp() {
        layerManager.getCurPath().lineTo(curX, curY)

        // draw a dot on click
        if (startX == curX && startY == curY) {
            layerManager.getCurPath().lineTo(curX, curY + 2)
            layerManager.getCurPath().lineTo(curX + 1, curY + 2)
            layerManager.getCurPath().lineTo(curX + 1, curY)
        }

        val curve = Curve(layerManager.getCurPath(), Paint().apply {
            color = paintOptions.color
            strokeWidth = paintOptions.strokeWidth
            alpha = paintOptions.alpha
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        })

        layerManager.setCurPath(DrawablePath())

        addElementToLayer(layerManager.activeLayerIndex, curve)
    }

    private fun drawHeart(centerX: Float, centerY: Float, outerRadius: Float) {
        val heart = Heart(
            centerX,
            centerY,
            outerRadius,
            Paint().apply {
                color = paintOptions.color
                strokeWidth = outerRadius
                alpha = paintOptions.alpha
                style = Paint.Style.FILL
            }
        )

        addElementToLayer(layerManager.activeLayerIndex, heart)
    }

    private fun drawStar(centerX: Float, centerY: Float, outerRadius: Float) {
        val star = Star(
            centerX,
            centerY,
            outerRadius,
            Paint().apply {
                color = paintOptions.color
                style = paintOptions.style
                strokeWidth = 6f
                alpha = paintOptions.alpha
            }
        )

        addElementToLayer(layerManager.activeLayerIndex, star)
    }

    fun drawImage(image: Int) {
        imageBitmap = BitmapFactory.decodeResource(resources, image)
        invalidate()
    }

    fun moveLayer(fromIndex: Int, toIndex: Int) {
        if (layerManager.moveLayer(fromIndex, toIndex)) {
            layerManager.activeLayerIndex = toIndex
            invalidate()
        }
    }

    fun removeLayer(layerIndex: Int) {
        layerManager.removeLayer(layerIndex)
        layerManager.activeLayerIndex = if (layerIndex == 0) 0 else layerIndex - 1
        invalidate()
    }


    private fun addElementToLayer(layerIndex: Int, element: Element) {
        val layerId = layerManager.addElementToLayer(layerIndex, element)
        if (layerId != null) {
            DrawHistoryHolder.addToHistory(AddElementToLayer(element, layerManager, layerId))
        }
        else {
            logger.error { "Adding element to the layer was not successfully" }
        }
    }
}