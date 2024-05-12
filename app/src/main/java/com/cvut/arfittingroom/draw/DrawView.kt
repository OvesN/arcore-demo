package com.cvut.arfittingroom.draw

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
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.controller.ScaleGestureDetector
import com.cvut.arfittingroom.draw.DrawHistoryHolder.addToHistory
import com.cvut.arfittingroom.draw.DrawHistoryHolder.clearHistory
import com.cvut.arfittingroom.draw.command.Repaintable
import com.cvut.arfittingroom.draw.command.action.element.impl.AddElementToLayer
import com.cvut.arfittingroom.draw.command.action.element.impl.MoveElement
import com.cvut.arfittingroom.draw.command.action.element.impl.RemoveElementFromLayer
import com.cvut.arfittingroom.draw.command.action.element.impl.RepaintElement
import com.cvut.arfittingroom.draw.command.action.element.impl.RotateElement
import com.cvut.arfittingroom.draw.command.action.element.impl.ScaleElement
import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.draw.model.element.Element
import com.cvut.arfittingroom.draw.model.element.impl.Curve
import com.cvut.arfittingroom.draw.model.element.impl.Gif
import com.cvut.arfittingroom.draw.model.element.impl.Image
import com.cvut.arfittingroom.draw.model.element.impl.Stamp
import com.cvut.arfittingroom.draw.model.element.strategy.PathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.HeartPathCreationStrategy
import com.cvut.arfittingroom.draw.model.element.strategy.impl.StarPathCreationStrategy
import com.cvut.arfittingroom.draw.model.enums.EElementEditAction
import com.cvut.arfittingroom.draw.model.enums.EDrawingMode
import com.cvut.arfittingroom.draw.path.DrawablePath
import com.cvut.arfittingroom.draw.service.TexturedBrushDrawer
import com.cvut.arfittingroom.draw.service.LayerManager
import com.cvut.arfittingroom.draw.service.UIDrawer
import com.cvut.arfittingroom.model.SPAN_SLOP
import com.cvut.arfittingroom.model.TOUCH_TO_MOVE_THRESHOLD
import com.cvut.arfittingroom.model.to.BrushTO
import com.cvut.arfittingroom.utils.BitmapUtil.adjustBitmapFromEditor
import com.cvut.arfittingroom.utils.FileUtil.deleteTempFiles
import com.cvut.arfittingroom.utils.FileUtil.saveTempMaskFrames
import com.cvut.arfittingroom.utils.FileUtil.saveTempMaskTextureBitmap
import com.cvut.arfittingroom.utils.UIUtil.showColorPickerDialog
import io.github.muddz.styleabletoast.StyleableToast
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifDrawableBuilder
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


/**
 * Draw view for 2D editor
 *
 *
 * @param context
 * @param attrs
 */
class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var paintOptions = PaintOptions()
    private var curX = 0f
    private var curY = 0f
    private var startX = 0f
    private var startY = 0f
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
    private var drawingMode = EDrawingMode.BRUSH
    private var stampType: PathCreationStrategy? = null
    private var ignoreNextOneFingerMove = false
    private val uiDrawer = UIDrawer(context)
    private var ignoreDrawing: Boolean = false
    private var canvasTransformationMatrix: Matrix = Matrix()
    private var handler = Handler(Looper.getMainLooper())
    private var gifRunnable: Runnable? = null
    private var frameDelay: Long = 100
    private var lastDownX = 0f
    private var lastDownY = 0f
    private var frameCount = 0


    @Inject
    lateinit var layerManager: LayerManager


    init {
        (context.applicationContext as? ARFittingRoomApplication)?.appComponent?.inject(this)

        // Add event for element scaling
        scaleGestureDetector =
            ScaleGestureDetector(
                context,
                object :
                    ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                        ignoreNextOneFingerMove = true
                        return super.onScaleBegin(detector)
                    }

                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        if (!isGestureWithinTolerance(detector)) {
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
                        selectedElement?.let { element ->
                            element.endContinuousScale()
                            addToHistory(
                                ScaleElement(
                                    element.id,
                                    element,
                                    newRadius = element.outerRadius * elementScaleFactor,
                                    oldRadius = element.outerRadius,
                                ),
                            )
                            ignoreNextOneFingerMove = true
                            elementScaleFactor = 1f
                        }
                        super.onScaleEnd(detector)
                    }

                    private fun isGestureWithinTolerance(detector: ScaleGestureDetector): Boolean {
                        val spanDelta = abs(detector.currentSpan - detector.previousSpan)
                        return spanDelta > SPAN_SLOP
                    }

                    private fun scaleSelectedElement(detector: ScaleGestureDetector) {
                        elementScaleFactor *= detector.scaleFactor
                        elementScaleFactor =
                            0.1f.coerceAtLeast(elementScaleFactor.coerceAtMost(10.0f))
                        selectedElement?.scaleContinuously(elementScaleFactor)
                    }

                    private fun scaleCanvas(detector: ScaleGestureDetector) {
                        canvasScaleFactor *= detector.scaleFactor
                        canvasScaleFactor =
                            0.1f.coerceAtLeast(canvasScaleFactor.coerceAtMost(10.0f))
                    }
                },
            )
    }

    fun setDimensions(
        width: Int,
        height: Int,
    ) {
        uiDrawer.setDimensions(width, height)
        layerManager.setDimensions(width, height)
    }

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

            MotionEvent.ACTION_MOVE ->
                if (!isDistanceGreaterThanThreshold(x, y)) {
                    return true
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
        } else {
            when (drawingMode) {
                EDrawingMode.NONE -> handleElementEditing(event, x, y)
                EDrawingMode.BRUSH -> handleDrawing(event, x, y)
                EDrawingMode.STAMP -> handleStampDrawing(event, x, y)
            }
        }

        invalidate()
        return true
    }

    private fun startAnimation(gif: Gif) {
        if (gifRunnable != null) {
            stopAnimation()
        }

        Log.println(Log.INFO, null, "Start animation")
        gifRunnable =
            Runnable {
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

            ACTION_POINTER_UP, MotionEvent.ACTION_UP ->
                if (event.pointerCount <= 2) {
                    lastTouchX = 0f
                    lastTouchY = 0f
                }
        }
    }

    private fun handleElementEditing(
        event: MotionEvent,
        x: Float,
        y: Float,
    ) {
        if (!scaleGestureDetector.isInProgress) {
            when (event.action) {
                ACTION_POINTER_UP, MotionEvent.ACTION_UP -> handleActionUp(event, x, y)
                MotionEvent.ACTION_DOWN -> handleActionDown(x, y)
                MotionEvent.ACTION_MOVE -> handleActionMove(x, y)
            }
        }
    }

    private fun handleActionUp(
        event: MotionEvent,
        x: Float,
        y: Float,
    ) {
        if (isInElementMovingMode) {
            finishElementMove(x, y)
        } else if (isInElementScalingMode) {
            finishElementScale()
        } else if (isInElementRotationMode) {
            finishElementRotation()
        } else if (event.pointerCount - 1 == 0) {
            ignoreNextOneFingerMove = false
        }
    }

    private fun handleActionDown(
        x: Float,
        y: Float,
    ) {
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
        selectedElement?.let {
            if (it is Gif) {
                startAnimation(it)
            }
        }
    }

    private fun handleActionMove(
        x: Float,
        y: Float,
    ) {
        selectedElement?.let { element ->
            if (isInElementScalingMode) {
                elementScaleFactor = calculateScaleFactor(x)
                element.scaleContinuously(elementScaleFactor)
            } else if (isInElementRotationMode) {
                rotationAngleDelta =
                    calculateRotationAngleDelta(
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

    private fun finishElementMove(
        x: Float,
        y: Float,
    ) {
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
                ),
            )
        }
        resetEditState()
    }

    private fun finishElementScale() {
        selectedElement?.let { element ->
            val newRadius = element.outerRadius
            element.endContinuousScale()
            addToHistory(
                ScaleElement(
                    element.id,
                    element,
                    newRadius = newRadius,
                    oldRadius = element.outerRadius,
                ),
            )
        }
        resetEditState()
    }

    private fun finishElementRotation() {
        selectedElement?.let { element ->
            val newRotationAngle = element.rotationAngle
            element.endContinuousRotation()
            addToHistory(
                RotateElement(
                    element.id,
                    element,
                    newRotationAngle = newRotationAngle,
                    oldRotationAngle = element.rotationAngle,
                ),
            )
        }
        resetEditState()
    }

    private fun calculateRotationAngleDelta(
        centerX: Float,
        centerY: Float,
        newX: Float,
        newY: Float,
    ): Float {
        // Calculate the angle from the center to the last touch position
        val lastAngle =
            Math.toDegrees(
                atan2(
                    (lastTouchY - centerY).toDouble(),
                    (lastTouchX - centerX).toDouble(),
                ),
            )

        // Calculate the angle from the center to the new touch position
        val newAngle =
            Math.toDegrees(atan2((newY - centerY).toDouble(), (newX - centerX).toDouble()))

        // Calculate the difference in angles
        var angleDelta = newAngle - lastAngle

        // Normalize the angleDelta to be between -180 and 180
        angleDelta +=
            if (angleDelta > 180) {
                -360
            } else if (angleDelta < -180) {
                360
            } else {
                0
            }

        return angleDelta.toFloat()
    }

    private fun isDistanceGreaterThanThreshold(
        x: Float,
        y: Float,
    ): Boolean {
        val deltaX = lastDownX - x
        val deltaY = lastDownY - y

        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)

        return distance > TOUCH_TO_MOVE_THRESHOLD
    }

    private fun calculateScaleFactor(x: Float): Float {
        val diff = x - lastTouchX
        // A positive diff means moving right, negative means moving left.
        val scaleFactor =
            if (diff > 0) {
                1 + abs(diff) / 100  // Increase the scale if moving to the right
            } else {
                1 - abs(diff) / 100  // Decrease the scale if moving to the left
            }

        scaleFactor.coerceIn(0.5f, 2f)

        return scaleFactor
    }

    private fun handleIconAction(
        action: EElementEditAction,
        element: Element,
    ) {
        when (action) {
            EElementEditAction.MENU -> isInElementMenuMode = !isInElementMenuMode

            EElementEditAction.SCALE -> isInElementScalingMode = true

            EElementEditAction.ROTATE -> isInElementRotationMode = true

            EElementEditAction.DELETE -> {
                addToHistory(
                    RemoveElementFromLayer(
                        element.id,
                        element,
                        layerManager,
                        layerManager.getActiveLayerId(),
                    ),
                )
                selectedElement = null
            }

            EElementEditAction.CHANGE_COLOR -> {
                selectedElement?.let { selectedElement ->
                    showColorPickerDialog(context, paintOptions.color) { envelopColor ->
                        repaintElement(selectedElement, envelopColor)
                    }
                }

                element.setSelected(false)
                selectedElement = null
            }

            EElementEditAction.MOVE_UP -> {
                val command =
                    layerManager.moveElementUp(
                        element,
                    )
                command?.let { addToHistory(command) }
                element.setSelected(false)
                selectedElement = null
            }

            EElementEditAction.MOVE_DOWN -> {
                val command =
                    layerManager.moveElementDown(
                        element,
                    )
                command?.let { addToHistory(command) }
                element.setSelected(false)
                selectedElement = null
            }
            // FIXME do not work, history do not work
            EElementEditAction.MOVE_TO -> {
                element.setSelected(false)
                selectedElement = null
                // TODO open menu with layers
                return
                val command = layerManager.moveElementTo(element, 1)
                command?.let { addToHistory(command) }
            }
        }
    }

    private fun handleDrawing(
        event: MotionEvent,
        x: Float,
        y: Float,
    ) {
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

    private fun handleStampDrawing(
        event: MotionEvent,
        x: Float,
        y: Float,
    ) {
        if (ignoreDrawing) {
            return
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            drawStamp(x, y, paintOptions.strokeWidth)
        }
    }

    fun undo() {
        val command = DrawHistoryHolder.undo()
        command?.let {
            StyleableToast.makeText(context, "Undo ${command.description}", R.style.mytoast).show()
        }
        stopAnimation()
        layerManager.updateLayersBitmaps()
        invalidate()
    }

    fun redo() {
        val command = DrawHistoryHolder.redo()
        command?.let {
            StyleableToast.makeText(context, "Redo ${command.description}", R.style.mytoast).show()
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

    fun setColor(newColor: Int) {
        @ColorInt
        paintOptions.color = newColor
        paintOptions.alpha = newColor.alpha


        if (paintOptions.strokeTextureRef.isNotEmpty()) {
            TexturedBrushDrawer.updateBrushTextureBitmap(
                paintOptions.strokeWidth.toInt(),
                paintOptions.color,
                paintOptions.alpha
            )
            //   TexturedCurveDrawer.changeBrushColor(paintOptions.color, paintOptions.alpha)
        }
    }

    fun setStrokeWidth(newStrokeWidth: Int) {
        paintOptions.strokeWidth = newStrokeWidth.toFloat()

        if (paintOptions.strokeTextureRef.isNotEmpty()) {
            TexturedBrushDrawer.updateBrushTextureBitmap(
                newStrokeWidth,
                paintOptions.color,
                paintOptions.alpha,
            )
        }
    }

    fun setBrush(brush: BrushTO, brushTexture: Bitmap? = null) {
        paintOptions.strokeTextureRef = brush.strokeTextureRef
        paintOptions.style = brush.style
        paintOptions.strokeCap = brush.strokeCap
        paintOptions.strokeJoint = brush.strokeJoint

        TexturedBrushDrawer.resetBitmaps()

        brushTexture?.let {
            TexturedBrushDrawer.setBrushBitmap(
                it,
                paintOptions.strokeWidth,
                paintOptions.color,
                paintOptions.alpha
            )
        }
    }

    fun setEditingMode() {
        drawingMode = EDrawingMode.NONE
        stampType = null
        TexturedBrushDrawer.resetBitmaps()
    }

    fun setStamp(pathCreationStrategy: PathCreationStrategy) {
        stampType = pathCreationStrategy
        drawingMode = EDrawingMode.STAMP
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
        shouldDrawBackground: Boolean = true,
    ) {
        if (shouldDrawBackground) {
            uiDrawer.drawBackground(canvas)
        }

        layerManager.drawLayers(canvas, paintOptions)
        uiDrawer.drawSelectedElementEditIcons(canvas, selectedElement, isInElementMenuMode)

        if (shouldDrawBackground) {
            uiDrawer.drawFaceTextureImage(canvas)
        }
    }

    private fun actionDown(
        x: Float,
        y: Float,
    ) {
        layerManager.getCurPath().reset()
        layerManager.getCurPath().moveTo(x, y)
        curX = x
        curY = y
    }

    private fun actionMove(
        x: Float,
        y: Float,
    ) {
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

        val curve =
            Curve(
                path = layerManager.getCurPath(),
                paint =
                Paint().apply {
                    color = paintOptions.color
                    strokeWidth = paintOptions.strokeWidth
                    alpha = paintOptions.alpha
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    style = Paint.Style.STROKE
                },
                bitmapTexture = TexturedBrushDrawer.originalBitmap,
                strokeTextureRef = paintOptions.strokeTextureRef
            )

        layerManager.setCurPath(DrawablePath())

        addElementToLayer(layerManager.getActiveLayerIndex(), curve)
    }

    private fun drawHeart(
        centerX: Float,
        centerY: Float,
        outerRadius: Float,
    ) {
        val heart =
            Stamp(
                centerX = centerX,
                centerY = centerY,
                outerRadius = outerRadius,
                pathCreationStrategy = HeartPathCreationStrategy(),
                paint =
                Paint().apply {
                    color = paintOptions.color
                    strokeWidth = outerRadius
                    alpha = paintOptions.alpha
                    style = Paint.Style.FILL
                },
            )

        addElementToLayer(layerManager.getActiveLayerIndex(), heart)
    }

    private fun drawStamp(
        centerX: Float,
        centerY: Float,
        outerRadius: Float,
    ) {
        stampType?.let {
            val stamp = Stamp(
                centerX = centerX,
                centerY = centerY,
                outerRadius = outerRadius,
                pathCreationStrategy = it,
                paint =
                Paint().apply {
                    color = paintOptions.color
                    style = paintOptions.style
                    strokeWidth = 6f
                    alpha = paintOptions.alpha
                },
            )

            addElementToLayer(layerManager.getActiveLayerIndex(), stamp)
        }
    }

    fun moveLayer(
        fromIndex: Int,
        toIndex: Int,
    ) {
        if (layerManager.canMoveLayer(fromIndex, toIndex)) {
            layerManager.setActiveLayer(toIndex)
            invalidate()
        }
    }

    fun removeLayer(layerIndex: Int) {
        layerManager.removeLayer(layerIndex)
        layerManager.setActiveLayer(if (layerIndex == 0) 0 else layerIndex - 1)
        invalidate()
    }

    private fun addElementToLayer(
        layerIndex: Int,
        element: Element,
    ) {
        val layerId = layerManager.getLayerIdByIndex(layerIndex)
        if (layerId != null) {
            addToHistory(AddElementToLayer(element.id, element, layerManager, layerId))
        } else {
            Log.println(Log.ERROR, null, "Adding element to the layer was unsuccessfull")
        }
    }

    fun addImage(bitmap: Bitmap, imageRef: String) {
        addElementToLayer(
            layerManager.getActiveLayerIndex(),
            Image(
                resourceRef = imageRef,
                centerX = width.toFloat() / 2,
                centerY = height.toFloat() / 2,
                outerRadius = width.toFloat() / 4,
            ).apply { this.bitmap = bitmap },
        )

        invalidate()
    }

    fun addGif(gifDrawable: GifDrawable, gifRef: String) {
        val gif =
            Gif(
                resourceRef = gifRef,
                centerX = width.toFloat() / 2,
                centerY = height.toFloat() / 2,
                outerRadius = width.toFloat() / 4,
            ).apply {
                setDrawable(gifDrawable)
                shouldDrawNextFrame = true
            }
        addElementToLayer(
            layerManager.getActiveLayerIndex(),
            gif,
        )

        layerManager.setUpdatableElement(gif)

        startAnimation(gif)
    }


    private fun resetEditState() {
        isInElementRotationMode = false
        isInElementScalingMode = false
        isInElementMovingMode = false

        rotationAngleDelta = 0f
        elementScaleFactor = 1f
    }

    fun saveBitmap(onSaved: () -> Unit) {
        deleteTempFiles(context)
        layerManager.deselectAllElements()
        if (layerManager.doesContainAnyGif()) {
            layerManager.setAllGifsToAnimationMode()
            layerManager.resetAllGifs()
            saveTempMaskFrames(layerManager, height, width, context) {
                onSaved()
            }
        } else {
            saveTempMaskTextureBitmap(
                adjustBitmapFromEditor(createBitmap(), height, width),
                context
            ) {
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

    // TODO not only color
    private fun repaintElement(
        element: Element,
        newColor: Int,
    ) {
        val repaintable = element as Repaintable

        addToHistory(
            RepaintElement(
                element.id,
                repaintable,
                oldColor = repaintable.paint.color,
                newColor = newColor,
            ),
        )

        invalidate()
    }

    private fun createCanvasTransformationMatrix(): Matrix {
        val transformationMatrix = Matrix()

        transformationMatrix.postTranslate(totalTranslateX, totalTranslateY)
        transformationMatrix.postScale(canvasScaleFactor, canvasScaleFactor, pivotX, pivotY)

        return transformationMatrix
    }

    fun applyBitmapBackground(bitmap: Bitmap?) {
        uiDrawer.setBackgroundBitmap(bitmap)
    }

}
