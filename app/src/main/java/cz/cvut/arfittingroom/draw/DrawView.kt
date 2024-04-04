package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.addToHistory
import cz.cvut.arfittingroom.draw.DrawHistoryHolder.clearHistory
import cz.cvut.arfittingroom.draw.command.Repaintable
import cz.cvut.arfittingroom.draw.command.action.element.impl.AddElementToLayer
import cz.cvut.arfittingroom.draw.command.action.element.impl.MoveElement
import cz.cvut.arfittingroom.draw.command.action.element.impl.RemoveElementFromLayer
import cz.cvut.arfittingroom.draw.command.action.element.impl.RepaintElement
import cz.cvut.arfittingroom.draw.command.action.element.impl.RotateElement
import cz.cvut.arfittingroom.draw.command.action.element.impl.ScaleElement
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
import cz.cvut.arfittingroom.utils.FileSavingUtil.saveTempMaskTextureBitmap
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
    private var paintOptions = PaintOptions()

    private var curX = 0f
    private var curY = 0f
    private var startX = 0f
    private var startY = 0f
    private var isStrokeWidthBarEnabled = false

    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector

    private var rotationAngleDelta = 0f

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var isInElementMovingMode: Boolean = false
    private var isInElementRotationMode: Boolean = false
    private var isInElementScalingMode: Boolean = false
    private var isInElementMenuMode: Boolean = false

    var selectedElement: Element? = null

    var strokeShape = EShape.CIRCLE

    private var ignoreNextOneFingerMove = false

    private val editElementIcons: HashMap<EElementEditAction, Bitmap> = hashMapOf()
    private val editElementIconsBounds: HashMap<EElementEditAction, RectF> = hashMapOf()
    private val menuBitmap: Bitmap


    //TODO MOVE MENU CONST
    private lateinit var displayMetrics: DisplayMetrics
    private var screenWidth = 0f
    private var screenHeight = 0f

    private var menuWidth = 0f
    private var menuHeight = 0f
    private var cornerRadius = 0f
    private var textSize = 0f
    private var textPadding = 0f
    private var lineSpacing = 0f
    private var menuItemSpacing = 0f

    //-----------------------------------

    interface OnLayerInitializedListener {
        fun onLayerInitialized(numOfLayers: Int)
    }


    private var layerInitializedListener: OnLayerInitializedListener? = null


    init {
        (context.applicationContext as? ARFittingRoomApplication)?.appComponent?.inject(this)

        // Prepare icons
        loadEditElementIcons()

        menuBitmap = prepareElementMenu()

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
                                it.id,
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


        // Post a runnable to the view, which will be executed after the view is laid out
        post {
            if (layerManager.getNumOfLayers() == 0) {
                layerManager.addLayer(width, height)

                layerInitializedListener?.onLayerInitialized(layerManager.getNumOfLayers())
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        // Handle multi-touch events for scaling
        //TODO handle deselecting while scaling
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
                                    element.id,
                                    movable = element,
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
                                    element.id,
                                    element,
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
                                    element.id,
                                    element,
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
                    isInElementMenuMode = false
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
                isInElementMenuMode = !isInElementMenuMode
            }

            EElementEditAction.SCALE -> {
                isInElementScalingMode = true
            }

            EElementEditAction.ROTATE -> {
                isInElementRotationMode = true
            }

            EElementEditAction.DELETE -> {
                addToHistory(
                    RemoveElementFromLayer(
                        element.id,
                        element,
                        layerManager,
                        layerManager.getActiveLayerId()
                    )
                )
                selectedElement = null
            }

            EElementEditAction.CHANGE_COLOR -> {
                showColorPickerDialog(true)
            }

            EElementEditAction.MOVE_UP -> {
                val command = layerManager.moveElementUp(
                    element,
                )
                command?.let { addToHistory(command) }
            }

            EElementEditAction.MOVE_DOWN -> {
                val command = layerManager.moveElementDown(
                    element,
                )
                command?.let { addToHistory(command) }
            }
            //FIXME do not work, history do not work
            EElementEditAction.MOVE_TO -> {
                //TODO open menu with layers
                return
                val command = layerManager.moveElementTo(element, 1)
                command?.let { addToHistory(command) }
            }
        }
    }

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


    fun addLayer(): Int {
        val newLayerIndex = layerManager.addLayer(width, height)
        layerManager.setActiveLayer(newLayerIndex)
        return newLayerIndex
    }

    //Return true if new layer is selected
    fun setActiveLayer(layerIndex: Int): Boolean {
        if (layerIndex >= layerManager.getNumOfLayers() || layerIndex < 0) {
            return false
        }
        layerManager.setActiveLayer(layerIndex)
        logger.info { "Active layer is now $layerIndex" }

        return true
    }

    private fun setColor(newColor: Int) {
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
        draw(canvas)

        return bitmap
    }

    fun setOnLayerInitializedListener(listener: OnLayerInitializedListener) {
        this.layerInitializedListener = listener
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

        addElementToLayer(layerManager.getActiveLayerIndex(), curve)
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

        addElementToLayer(layerManager.getActiveLayerIndex(), heart)
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

        addElementToLayer(layerManager.getActiveLayerIndex(), star)
    }

    fun moveLayer(fromIndex: Int, toIndex: Int) {
        if (layerManager.moveLayer(fromIndex, toIndex)) {
            layerManager.setActiveLayer(toIndex)
            invalidate()
        }
    }

    fun removeLayer(layerIndex: Int) {
        layerManager.removeLayer(layerIndex)
        layerManager.setActiveLayer(if (layerIndex == 0) 0 else layerIndex - 1)
        invalidate()
    }

    private fun addElementToLayer(layerIndex: Int, element: Element) {
        val layerId = layerManager.addElementToLayer(layerIndex, element)
        if (layerId != null) {
            addToHistory(AddElementToLayer(element.id, element, layerManager, layerId))
        } else {
            logger.error { "Adding element to the layer was not successfully" }
        }
    }

    fun loadImage(imageId: Int) {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, imageId, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width / 3, height / 3)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        val imageBitmap = BitmapFactory.decodeResource(resources, imageId, options)

        addElementToLayer(
            layerManager.getActiveLayerIndex(),
            Image(
                width.toFloat() / 2,
                height.toFloat() / 2,
                width.toFloat() / 2,
                imageId,
            ).apply { bitmap = imageBitmap }
        )

        invalidate()
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    //TODO move rectF creation
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

    //TODO to constants or config why???
    private fun prepareElementMenu(): Bitmap {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        val menuWidth = screenWidth * 0.3f * 1.3f
        val menuHeight = screenHeight * 0.125f * 1.3f
        val cornerRadius = screenWidth * 0.02f * 1.3f
        val textSize = screenHeight * 0.02f * 1.3f
        val textPadding = screenWidth * 0.025f * 1.3f
        val lineSpacing = screenHeight * 0.005f * 1.3f
        val menuItemSpacing = screenHeight * 0.025f * 1.3f

        //FIXME WHY??
        this.displayMetrics = displayMetrics
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight

        this.menuWidth = menuWidth
        this.menuHeight = menuHeight
        this.cornerRadius = cornerRadius
        this.textSize = textSize
        this.textPadding = textPadding
        this.lineSpacing = lineSpacing
        this.menuItemSpacing = menuItemSpacing

        val menuPaint = Paint().apply {
            color = Color.DKGRAY
            alpha = (255 * 0.9).toInt()
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            this.textSize = textSize
            textAlign = Paint.Align.LEFT
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }

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


    private fun resetEditState() {
        isInElementRotationMode = false
        isInElementScalingMode = false
        isInElementMovingMode = false

        rotationAngleDelta = 0f
        scaleFactor = 1f
    }

    fun saveBitmap(onSaved: () -> Unit) {
        layerManager.deselectAllElements()
        saveTempMaskTextureBitmap(adjustBitmap(createBitmap()), context) {
            onSaved()
        }
    }

    private fun createBitmap(): Bitmap {
        layerManager.deselectAllElements()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)

        return bitmap
    }

    private fun adjustBitmap(bitmap: Bitmap): Bitmap {
        // Calculate the dimensions for the square crop
        val newY = (height - width) / 2

        // Crop the bitmap
        val croppedBitmap = Bitmap.createBitmap(bitmap, 0, newY, width, width)

        // Create a matrix for the mirroring transformation
        val matrix = Matrix().apply {
            postScale(
                -1f,
                1f,
                croppedBitmap.width / 2f,
                croppedBitmap.height / 2f
            )
        }

        // Create and return the mirrored bitmap
        val mirroredBitmap = Bitmap.createBitmap(
            croppedBitmap,
            0,
            0,
            croppedBitmap.width,
            croppedBitmap.height,
            matrix,
            true
        )

        return Bitmap.createScaledBitmap(mirroredBitmap, 1024, 1024, true)
    }

    fun showColorPickerDialog(setElementColor: Boolean = false) {
        ColorPickerDialog.Builder(context)
            .setTitle("ColorPicker Dialog")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(R.string.OK,
                ColorEnvelopeListener { envelope, _ ->
                    run {
                        if (setElementColor) {
                            selectedElement?.let { element ->
                                repaintElement(element, envelope.color)
                            }
                        }
                        //Set brush color
                        else {
                            setColor(envelope.color)
                        }
                    }
                })
            .setNegativeButton(
                R.string.cancel,
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    //TODO not only color
    private fun repaintElement(element: Element, newColor: Int) {
        val repaintable = element as Repaintable

        addToHistory(
            RepaintElement(
                element.id,
                repaintable,
                oldColor = repaintable.paint.color,
                newColor = newColor
            )
        )
    }
}