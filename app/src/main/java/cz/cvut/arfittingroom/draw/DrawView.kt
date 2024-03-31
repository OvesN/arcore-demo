package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.addToHistory
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.clearHistory
import cz.cvut.arfittingroom.draw.command.Scalable
import cz.cvut.arfittingroom.draw.command.action.AddElementToLayer
import cz.cvut.arfittingroom.draw.command.action.MoveElement
import cz.cvut.arfittingroom.draw.command.action.RemoveElementFromLayer
import cz.cvut.arfittingroom.draw.command.action.RotateElement
import cz.cvut.arfittingroom.draw.command.action.ScaleElement
import cz.cvut.arfittingroom.draw.model.PaintOptions
import cz.cvut.arfittingroom.draw.model.element.Element
import cz.cvut.arfittingroom.draw.model.element.impl.Figure
import cz.cvut.arfittingroom.draw.model.element.impl.Curve
import cz.cvut.arfittingroom.draw.model.element.impl.Image
import cz.cvut.arfittingroom.draw.model.element.strategy.impl.HeartPathCreationStrategy
import cz.cvut.arfittingroom.draw.model.element.strategy.impl.StarPathCreationStrategy
import cz.cvut.arfittingroom.draw.model.enums.EElementEditAction
import cz.cvut.arfittingroom.draw.model.enums.EShape
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.service.LayerManager
import cz.cvut.arfittingroom.utils.IconUtil.changeIconColor
import mu.KotlinLogging
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2


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

    private var rotationAngleDelta = 0f

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    private var isInElementMovingMode: Boolean = false
    private var isInElementRotationMode: Boolean = false
    private var isInElementScalingMode: Boolean = false

    private var imageBitmap: Bitmap? = null
    var selectedElement: Element? = null

    var strokeShape = EShape.CIRCLE

    private var ignoreNextOneFingerMove = false

    private val editElementIcons: HashMap<EElementEditAction, Bitmap> = hashMapOf()
    private val editElementIconsBounds: HashMap<EElementEditAction, RectF> = hashMapOf()

    init {
        (context.applicationContext as? ARFittingRoomApplication)?.appComponent?.inject(this)

        // Prepare icons
        loadEditElementIcons()

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
                        scaleFactor = 0.1f.coerceAtLeast(scaleFactor.coerceAtMost(10.0f))

                        selectedElement?.scaleContinuously(scaleFactor)
                        invalidate()
                        return true
                    }
                    return false
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    selectedElement?.let {
                        it.endContinuousScale()
                        addToHistory(
                            ScaleElement(
                                it,
                                newRadius = it.outerRadius * scaleFactor,
                                oldRadius = it.outerRadius
                            )
                        )
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
            EShape.NONE -> handleElementEditing(event, x, y)
            EShape.CIRCLE -> handleDrawing(event, x, y)
            else -> handleStampDrawing(event, x, y)
        }

        invalidate()
        return true
    }

    private fun handleElementEditing(event: MotionEvent, x: Float, y: Float) {
        if (!scaleGestureDetector.isInProgress) {
            when (event.action) {
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                    if (isInElementMovingMode) {
                        selectedElement?.let { element ->
                            element.endContinuousMove()
                            addToHistory(
                                MoveElement(
                                    element = element,
                                    oldX = element.centerX,
                                    oldY = element.centerY,
                                    newX = x,
                                    newY = y,
                                )
                            )
                        }
                        resetEditState()

                    } else if (isInElementScalingMode) {
                        selectedElement?.let { element ->
                            val newRadius = element.outerRadius
                            element.endContinuousScale()
                            addToHistory(
                                ScaleElement(
                                    element = element,
                                    newRadius = newRadius,
                                    oldRadius = element.outerRadius,
                                )
                            )
                        }
                        resetEditState()
                    } else if (isInElementRotationMode) {
                        selectedElement?.let { element ->
                            val newRotationAngle = element.rotationAngle
                            element.endContinuousRotation()

                            addToHistory(
                                RotateElement(
                                    element = element,
                                    newRotationAngle = newRotationAngle,
                                    oldRotationAngle = element.rotationAngle
                                )
                            )
                        }
                        resetEditState()
                    }
                    // If user was scaling the element with two fingers using ScaleGestureDetector,
                    // ignore next one finger move so element will not be moved
                    else if (event.pointerCount - 1 == 0) {
                        ignoreNextOneFingerMove = false
                    }

                }

                MotionEvent.ACTION_DOWN -> {
                    resetEditState()
                    selectedElement?.let { element ->
                        val pressedIconAction = checkEditButtons(x, y)
                        if (pressedIconAction != null) {
                            lastTouchY = y
                            lastTouchX = x
                            handleIconAction(pressedIconAction, element)
                            return
                        }
                    }

                    selectedElement = layerManager.selectElement(x, y)
                }

                MotionEvent.ACTION_MOVE -> {
                    selectedElement?.let { element ->
                        if (isInElementScalingMode) {
                            scaleFactor = calculateScaleFactor(x, y)
                            element.scaleContinuously(scaleFactor)
                        } else if (isInElementRotationMode) {
                            rotationAngleDelta = calculateRotationAngleDelta(
                                centerX = element.centerX,
                                centerY = element.centerY,
                                newX = x,
                                newY = y,
                            )
                            element.rotateContinuously(rotationAngleDelta)
                        } else if (!ignoreNextOneFingerMove) {
                            isInElementMovingMode = true
                            element.move(x, y)
                        }
                    }
                }

            }
        }
    }

    private fun calculateRotationAngleDelta(
        centerX: Float,
        centerY: Float,
        newX: Float,
        newY: Float
    ): Float {
        // Calculate the angle from the center to the last touch position
        val lastAngle =
            Math.toDegrees(
                atan2(
                    (lastTouchY - centerY).toDouble(),
                    (lastTouchX - centerX).toDouble()
                )
            )

        // Calculate the angle from the center to the new touch position
        val newAngle =
            Math.toDegrees(atan2((newY - centerY).toDouble(), (newX - centerX).toDouble()))

        // Calculate the difference in angles
        var angleDelta = newAngle - lastAngle

        // Normalize the angleDelta to be between -180 and 180
        angleDelta += if (angleDelta > 180) -360 else if (angleDelta < -180) 360 else 0

        return angleDelta.toFloat()
    }

    private fun calculateScaleFactor(x: Float, y: Float): Float {
        val xDiff = x - lastTouchX
        // A positive xDiff means moving right, negative means moving left.
        val scaleFactor = if (xDiff > 0) {
            1 + abs(xDiff) / 100 // Increase the scale if moving to the right
        } else {
            1 - abs(xDiff) / 100 // Decrease the scale if moving to the left
        }

        scaleFactor.coerceIn(0.5f, 2f)

        return scaleFactor
    }


    private fun handleIconAction(action: EElementEditAction, element: Element) {
        when (action) {
            EElementEditAction.MENU -> {
                // Handle menu action
            }

            EElementEditAction.SCALE -> {
                isInElementScalingMode = true
            }

            EElementEditAction.MOVE_TO,
            EElementEditAction.MOVE_DOWN,
            EElementEditAction.MOVE_UP -> {
                // Handle move actions, if required
            }

            EElementEditAction.ROTATE -> {
                isInElementRotationMode = true
            }

            EElementEditAction.DELETE -> {
                addToHistory(
                    RemoveElementFromLayer(
                        element,
                        layerManager,
                        layerManager.getActiveLayerId()
                    )
                )
                selectedElement = null
            }
        }
    }


    // Check if edit button was pressed, if not, return null
    private fun checkEditButtons(x: Float, y: Float): EElementEditAction? =
        editElementIconsBounds.entries.firstOrNull { it.value.contains(x, y) }?.key


    private fun handleDrawing(event: MotionEvent, x: Float, y: Float) {
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

    private fun handleStampDrawing(event: MotionEvent, x: Float, y: Float) {
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

        drawSelectedElementEditIcons(canvas)
    }

    fun clearCanvas() {
        layerManager.deleteLayers()
        clearHistory()
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

        // Draw a dot on click
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
        val heart = Figure(
            centerX,
            centerY,
            outerRadius,
            HeartPathCreationStrategy(),
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
        val star = Figure(
            centerX,
            centerY,
            outerRadius,
            StarPathCreationStrategy(),
            Paint().apply {
                color = paintOptions.color
                style = paintOptions.style
                strokeWidth = 6f
                alpha = paintOptions.alpha
            }
        )

        addElementToLayer(layerManager.activeLayerIndex, star)
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
            addToHistory(AddElementToLayer(element, layerManager, layerId))
        } else {
            logger.error { "Adding element to the layer was not successfully" }
        }
    }

    fun loadImage(imageId: Int) {
        val imageBitmap = BitmapFactory.decodeResource(resources, imageId)

        addElementToLayer(
            layerManager.activeLayerIndex,
            Image(
                width.toFloat() / 2,
                height.toFloat() / 2,
                width.toFloat() / 2,
                imageId,
                imageBitmap
            )
        )

        invalidate()
    }

    private fun drawSelectedElementEditIcons(canvas: Canvas) {
        selectedElement?.let { element ->

            // Do not draw anything if element was deselected
            // previously (for example in undo/redo action)
            if (!element.isSelected) {
                selectedElement = null
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
        }
    }

    private fun loadEditElementIcons() {
        editElementIcons[EElementEditAction.DELETE] =
            changeIconColor(
                BitmapFactory.decodeResource(context.resources, R.drawable.delete_icon),
                Color.YELLOW
            )
        editElementIcons[EElementEditAction.ROTATE] =
            changeIconColor(
                BitmapFactory.decodeResource(context.resources, R.drawable.rotate_icon),
                Color.YELLOW
            )
        editElementIcons[EElementEditAction.SCALE] =
            changeIconColor(
                (BitmapFactory.decodeResource(context.resources, R.drawable.scale_icon)),
                Color.YELLOW
            )
        editElementIcons[EElementEditAction.MENU] =
            changeIconColor(
                (BitmapFactory.decodeResource(context.resources, R.drawable.menu_icon)),
                Color.YELLOW
            )
    }

    private fun resetEditState() {
        isInElementRotationMode = false
        isInElementScalingMode = false
        isInElementMovingMode = false

        rotationAngleDelta = 0f
        scaleFactor = 1f
    }

}