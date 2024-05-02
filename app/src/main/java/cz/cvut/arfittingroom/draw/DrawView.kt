package cz.cvut.arfittingroom.draw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.controller.ScaleGestureDetector
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
import cz.cvut.arfittingroom.draw.model.element.impl.Curve
import cz.cvut.arfittingroom.draw.model.element.impl.Figure
import cz.cvut.arfittingroom.draw.model.element.impl.Gif
import cz.cvut.arfittingroom.draw.model.element.impl.Image
import cz.cvut.arfittingroom.draw.model.element.strategy.impl.HeartPathCreationStrategy
import cz.cvut.arfittingroom.draw.model.element.strategy.impl.StarPathCreationStrategy
import cz.cvut.arfittingroom.draw.model.enums.EElementEditAction
import cz.cvut.arfittingroom.draw.model.enums.EShape
import cz.cvut.arfittingroom.draw.path.DrawablePath
import cz.cvut.arfittingroom.draw.service.LayerManager
import cz.cvut.arfittingroom.draw.service.UIDrawer
import cz.cvut.arfittingroom.model.SPAN_SLOP
import cz.cvut.arfittingroom.model.TOUCH_TO_MOVE_THRESHOLD
import cz.cvut.arfittingroom.utils.FileUtil.adjustBitmap
import cz.cvut.arfittingroom.utils.FileUtil.saveTempMaskFrames
import cz.cvut.arfittingroom.utils.FileUtil.saveTempMaskTextureBitmap
import mu.KotlinLogging
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifDrawableBuilder
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


private val logger = KotlinLogging.logger { }


class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    @Inject
    lateinit var layerManager: LayerManager
    private var paintOptions = PaintOptions()

    private var curX = 0f
    private var curY = 0f
    private var startX = 0f
    private var startY = 0f
    private var isStrokeWidthBarEnabled = false

    private var elementScaleFactor = 1.0f
    private var canvasScaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector

    private var rotationAngleDelta = 0f

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var totalTranslateX: Float = 0f
    private var totalTranslateY: Float = 0f

    private var isInElementMovingMode: Boolean = false
    private var isInElementRotationMode: Boolean = false
    private var isInElementScalingMode: Boolean = false
    private var isInElementMenuMode: Boolean = false

    var selectedElement: Element? = null
    var strokeShape = EShape.CIRCLE
    private var ignoreNextOneFingerMove = false

    private val uiDrawer = UIDrawer(context)

    private var ignoreDrawing: Boolean = false
    private var canvasTransformationMatrix: Matrix = Matrix()

    private var handler = Handler(Looper.getMainLooper())
    private var gifRunnable: Runnable? = null
    private var frameDelay: Long = 100

    interface OnLayerInitializedListener {
        fun onLayerInitialized(numOfLayers: Int)
    }

    private var layerInitializedListener: OnLayerInitializedListener? = null

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
                    if (!gestureTolerance(detector)) {
                        return false
                    }
                    if (selectedElement != null) {
                        scaleSelectedElement(detector)
                    } else {
                        scaleCanvas(detector)
                    }

                    invalidate()
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    selectedElement?.let {
                        it.endContinuousScale()
                        addToHistory(
                            ScaleElement(
                                it.id,
                                it,
                                newRadius = it.outerRadius * elementScaleFactor,
                                oldRadius = it.outerRadius
                            )
                        )
                        ignoreNextOneFingerMove = true
                        elementScaleFactor = 1f
                    }
                    super.onScaleEnd(detector)
                }

                private fun gestureTolerance(detector: ScaleGestureDetector): Boolean {
                    val spanDelta = abs(detector.currentSpan - detector.previousSpan)
                    return spanDelta > SPAN_SLOP
                }

                private fun scaleSelectedElement(detector: ScaleGestureDetector) {
                    elementScaleFactor *= detector.scaleFactor
                    elementScaleFactor = 0.1f.coerceAtLeast(elementScaleFactor.coerceAtMost(10.0f))
                    selectedElement?.scaleContinuously(elementScaleFactor)
                }

                private fun scaleCanvas(detector: ScaleGestureDetector) {
                    canvasScaleFactor *= detector.scaleFactor
                    canvasScaleFactor = 0.1f.coerceAtLeast(canvasScaleFactor.coerceAtMost(10.0f))
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

    private var lastDownX = 0f
    private var lastDownY = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val inverseMatrix = Matrix()
        if (!canvasTransformationMatrix.invert(inverseMatrix)) {
            return false
        }

        val touchPoint = floatArrayOf(event.x, event.y)
        inverseMatrix.mapPoints(touchPoint)
        val x = touchPoint[0]
        val y = touchPoint[1]

        // Some devices do not distinguish MOVE and just screen touching events, so we need to control it manually
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastDownX = x
                lastDownY = y
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isDistanceGreaterThanThreshold(x, y)) {
                    return true
                }
            }

            MotionEvent.ACTION_UP, ACTION_POINTER_UP -> {
                lastDownX = 0f
                lastDownY = 0f
            }
        }

        // Handle multi-touch events for scaling
        if (event.pointerCount == 2) {
            ignoreDrawing = true
            layerManager.setCurPath(DrawablePath())
            scaleGestureDetector.onTouchEvent(event)
            if (selectedElement == null) {
                handleCanvasGesture(event)
            }
        } else if (ignoreDrawing &&
            (event.actionMasked == ACTION_POINTER_UP || event.actionMasked == MotionEvent.ACTION_UP)
        ) {
            ignoreDrawing = false
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

    private var frameCount = 0
    private fun startAnimation(gif: Gif) {
        if (gifRunnable != null) {
            stopAnimation()
        }

        Log.println(Log.INFO, null, "Start animation")
        gifRunnable = Runnable {
            Log.println(Log.INFO, null, "count $frameCount")
            // Play gif three times and stop on the first frame
            if (frameCount >= gif.gifDrawable.numberOfFrames * 3 && gif.currentFrameIndex == 0
            ) {
                frameCount = 0
                stopAnimation(gif)
            } else {
                gif.currentFrameIndex++
                frameCount++
                invalidate()
                handler.postDelayed(gifRunnable!!, frameDelay)
            }
        }
        gifRunnable?.let { handler.post(it) }
    }

    fun stopAnimation(gif: Gif? = null) {
        if (gif != null) {
            gif.shouldDrawNextFrame = false
            gif.currentFrameIndex = 0
        }
        Log.println(Log.INFO, null, "Stop animation")
        gifRunnable?.let {
            handler.removeCallbacks(it)
            gifRunnable = null
        }
    }

    private fun handleCanvasGesture(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val currentX = (event.getX(0) + event.getX(1)) / 2
                val currentY = (event.getY(0) + event.getY(1)) / 2

                // Calculate the translation delta
                val dx = currentX - lastTouchX
                val dy = currentY - lastTouchY

                if (!scaleGestureDetector.isInProgress && lastTouchX != 0f && lastTouchY != 0f) {
                    totalTranslateX += dx
                    totalTranslateY += dy
                }

                lastTouchX = currentX
                lastTouchY = currentY
            }

            ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                if (event.pointerCount <= 2) {
                    lastTouchX = 0f
                    lastTouchY = 0f
                }
            }
        }
    }

    private fun handleElementEditing(event: MotionEvent, x: Float, y: Float) {
        if (!scaleGestureDetector.isInProgress) {
            when (event.action) {
                ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
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
                        val pressedIconAction = uiDrawer.checkEditButtons(x, y)
                        if (pressedIconAction != null) {
                            lastTouchY = y
                            lastTouchX = x
                            handleIconAction(pressedIconAction, element)
                            return
                        }
                    }

                    selectedElement = layerManager.selectElement(x, y)
                    // In case that we selected gif, play it's animation
                    selectedElement.let {
                        if (it is Gif) {
                            startAnimation(it)
                        }
                    }
                    isInElementMenuMode = false
                }

                MotionEvent.ACTION_MOVE -> {
                    selectedElement?.let { element ->
                        if (isInElementScalingMode) {
                            elementScaleFactor = calculateScaleFactor(x, y)
                            element.scaleContinuously(elementScaleFactor)
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

    private fun isDistanceGreaterThanThreshold(x2: Float, y2: Float): Boolean {
        val deltaX = lastDownX - x2
        val deltaY = lastDownY - y2

        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)

        return distance > TOUCH_TO_MOVE_THRESHOLD
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
                element.setSelected(false)
                selectedElement = null
                showColorPickerDialog(true)
            }

            EElementEditAction.MOVE_UP -> {
                val command = layerManager.moveElementUp(
                    element,
                )
                command?.let { addToHistory(command) }
                element.setSelected(false)
                selectedElement = null
            }

            EElementEditAction.MOVE_DOWN -> {
                val command = layerManager.moveElementDown(
                    element,
                )
                command?.let { addToHistory(command) }
                element.setSelected(false)
                selectedElement = null
            }
            //FIXME do not work, history do not work
            EElementEditAction.MOVE_TO -> {
                element.setSelected(false)
                selectedElement = null
                //TODO open menu with layers
                return
                val command = layerManager.moveElementTo(element, 1)
                command?.let { addToHistory(command) }

            }
        }
    }

    private fun handleDrawing(event: MotionEvent, x: Float, y: Float) {
        if (ignoreDrawing) {
            return
        }
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
        if (ignoreDrawing) {
            return
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (strokeShape) {
                EShape.STAR -> drawStar(x, y, paintOptions.strokeWidth)
                EShape.HEART -> drawHeart(x, y, paintOptions.strokeWidth)
                else -> {}
            }
        }
    }

    fun undo() {
        val command = DrawHistoryHolder.undo()
        command?.let {
            val toast = Toast.makeText(context, "Undo ${command.description}", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)

            toast.show()
        }
        stopAnimation()
        layerManager.updateLayersBitmaps()
        invalidate()
    }

    fun redo() {
        val command = DrawHistoryHolder.redo()
        command?.let {
            val toast = Toast.makeText(context, "Redo ${command.description}", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)

            toast.show()
        }
        stopAnimation()
        layerManager.updateLayersBitmaps()
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

    fun setOnLayerInitializedListener(listener: OnLayerInitializedListener) {
        this.layerInitializedListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvasTransformationMatrix = createCanvasTransformationMatrix()
        canvas.setMatrix(canvasTransformationMatrix)
        draw(canvas, true)
    }

    fun clearCanvas() {
        layerManager.deleteLayers()
        clearHistory()
        // Add initial layer
        layerManager.addLayer(width, height)
        invalidate()
    }

    private fun draw(
        canvas: Canvas,
        shouldDrawFaceTexture: Boolean = true
    ) {
        layerManager.drawLayers(canvas, paintOptions)

        uiDrawer.drawSelectedElementEditIcons(canvas, selectedElement, isInElementMenuMode)
        if (shouldDrawFaceTexture) {
            uiDrawer.drawFaceTextureImage(canvas)
        }
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
        val layerId = layerManager.getLayerIdByIndex(layerIndex)
        if (layerId != null) {
            addToHistory(AddElementToLayer(element.id, element, layerManager, layerId))
        } else {
            logger.error { "Adding element to the layer was unsuccessfull" }
        }
    }


    fun loadImage(imageId: Int) {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, imageId, options)

        // Calculate inSampleSize
        options.inSampleSize =
            calculateInSampleSize(options.outWidth, options.outHeight, width / 3, height / 3)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        val imageBitmap = BitmapFactory.decodeResource(resources, imageId, options)

        addElementToLayer(
            layerManager.getActiveLayerIndex(),
            Image(
                width.toFloat() / 2,
                height.toFloat() / 2,
                width.toFloat() / 4,
                imageId,
            ).apply { bitmap = imageBitmap }
        )

        invalidate()
    }

    fun loadGif(gifId: Int) {
        val gifDrawable = GifDrawable(resources, gifId)

        val adjustedGif = GifDrawableBuilder().with(gifDrawable).from(resources, gifId).sampleSize(
            calculateInSampleSize(
                gifDrawable.currentFrame.width,
                gifDrawable.currentFrame.height,
                width / 3,
                height / 3
            )
        ).build()

        val gif = Gif(
            gifId,
            width.toFloat() / 2,
            height.toFloat() / 2,
            width.toFloat() / 4,
        ).apply {
            setDrawable(adjustedGif)
            shouldDrawNextFrame = true
        }
        addElementToLayer(
            layerManager.getActiveLayerIndex(),
            gif
        )

        layerManager.setUpdatableElement(gif)

        startAnimation(gif)
    }


    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
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

    private fun resetEditState() {
        isInElementRotationMode = false
        isInElementScalingMode = false
        isInElementMovingMode = false

        rotationAngleDelta = 0f
        elementScaleFactor = 1f
    }

    fun saveBitmap(onSaved: () -> Unit) {
        layerManager.deselectAllElements()
        if (layerManager.doesContainAnyGif()) {
            layerManager.setAllGifsToAnimationMode()
            layerManager.resetAllGifs()
            saveTempMaskFrames(layerManager, height, width, context) {
                onSaved()
            }
        } else {
            saveTempMaskTextureBitmap(adjustBitmap(createBitmap(), height, width), context) {
                onSaved()
            }
        }

    }

    private fun createBitmap(): Bitmap {
        layerManager.deselectAllElements()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas, false)

        return bitmap
    }

    //TODO REFACTOR
    fun showColorPickerDialog(setElementColor: Boolean = false) {
        ColorPickerDialog.Builder(context)
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

        invalidate()
    }


    private fun createCanvasTransformationMatrix(): Matrix {
        val transformationMatrix = Matrix()

        transformationMatrix.postTranslate(totalTranslateX, totalTranslateY)
        transformationMatrix.postScale(canvasScaleFactor, canvasScaleFactor, pivotX, pivotY)

        return transformationMatrix
    }


}