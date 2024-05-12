package com.cvut.arfittingroom.activity

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cvut.arfittingroom.ARFittingRoomApplication
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.databinding.ActivityShowRoomBinding
import com.cvut.arfittingroom.draw.DrawHistoryHolder
import com.cvut.arfittingroom.fragment.AccessoriesOptionsFragment
import com.cvut.arfittingroom.fragment.CameraModeFragment
import com.cvut.arfittingroom.fragment.LookOptionsMenuFragment
import com.cvut.arfittingroom.fragment.MakeupOptionsMenuFragment
import com.cvut.arfittingroom.fragment.MaskEditorFragment
import com.cvut.arfittingroom.fragment.ProfileFragment
import com.cvut.arfittingroom.model.LOOKS_COLLECTION
import com.cvut.arfittingroom.model.MAKEUP_SLOT
import com.cvut.arfittingroom.model.MASK_FRAME_FILE_NAME
import com.cvut.arfittingroom.model.MASK_TEXTURE_SLOT
import com.cvut.arfittingroom.model.MAX_LOOK_NAME_LENGTH
import com.cvut.arfittingroom.model.PREVIEW_BITMAP_SIZE
import com.cvut.arfittingroom.model.PREVIEW_COLLECTION
import com.cvut.arfittingroom.model.to.LookTO
import com.cvut.arfittingroom.model.to.MakeupTO
import com.cvut.arfittingroom.model.to.ModelTO
import com.cvut.arfittingroom.service.StateService
import com.cvut.arfittingroom.utils.BitmapUtil.combineBitmaps
import com.cvut.arfittingroom.utils.FileUtil.deleteTempFiles
import com.cvut.arfittingroom.utils.FileUtil.doesTempAnimatedMaskExist
import com.cvut.arfittingroom.utils.FileUtil.getNextTempMaskFrame
import com.cvut.arfittingroom.utils.FileUtil.getNextTempMaskFrameInputStream
import com.cvut.arfittingroom.utils.FileUtil.getNumberOfFrames
import com.cvut.arfittingroom.utils.FileUtil.getTempMaskTextureBitmap
import com.cvut.arfittingroom.utils.FileUtil.getTempMaskTextureStream
import com.cvut.arfittingroom.utils.UIUtil.showClearAllDialog
import com.cvut.arfittingroom.utils.currentUserUsername
import com.google.android.filament.LightManager
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFrontFacingFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gorisse.thomas.sceneform.environment
import com.gorisse.thomas.sceneform.light.LightEstimationConfig
import com.gorisse.thomas.sceneform.light.build
import com.gorisse.thomas.sceneform.lightEstimationConfig
import com.gorisse.thomas.sceneform.mainLight
import io.github.muddz.styleabletoast.StyleableToast
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

class ShowRoomActivity :
    AppCompatActivity(),
    ResourceListener,
    UIChangeListener {
    private val binding: ActivityShowRoomBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private val gifTextures = mutableListOf<Texture>()
    private var frameCounter = 0
    private var handler = Handler(Looper.getMainLooper())
    private var gifRunnable: Runnable? = null

    private var frameDelay: Long = 100  // Default frame delay (100 ms per frame)
    private val accessoriesOptionsFragment = AccessoriesOptionsFragment()
    private val looksOptionsFragment = LookOptionsMenuFragment()
    private val makeupOptionsFragment = MakeupOptionsMenuFragment()
    private val cameraModeFragment = CameraModeFragment()
    private val profileFragment = ProfileFragment()
    private val maskEditorFragment = MaskEditorFragment()
    private lateinit var arFragment: ArFrontFacingFragment
    private lateinit var arSceneView: ArSceneView
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    @Inject
    lateinit var stateService: StateService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }

        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setContentView(binding.root)

        supportFragmentManager.addFragmentOnAttachListener { fragmentManager: FragmentManager, fragment: Fragment ->
            this.onAttachFragment(
                fragmentManager,
                fragment,
            )
        }

        if (savedInstanceState == null) {
            addMenuFragments()
            showMakeupOptionsMenu()

            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFrontFacingFragment::class.java, null)
                    .commit()
            } else {
                StyleableToast.makeText(this, "Sceneform is not supported", R.style.mytoast)
                finish()
            }
        }

        binding.makeupButton.setOnClickListener {
            showMakeupOptionsMenu()
        }
        binding.accessoriesButton.setOnClickListener {
            showAccessoriesMenu()
        }
        binding.looksButton.setOnClickListener {
            showLooksMenu()
        }
        binding.saveButton.setOnClickListener {
            showSaveLookDialog()
        }
        binding.deleteButton.setOnClickListener {
            showClearAllDialog(this) { clearAll() }
        }
        binding.cameraModeButton.setOnClickListener {
            showCameraModeUI()
        }
        binding.profileButton.setOnClickListener {
            showProfileUI()
        }
        binding.maskEditorButton.setOnClickListener {
            showMakeupEditorUI()
            arSceneView.pause()
        }
    }

    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }

    override fun onResume() {
        super.onResume()
        arSceneView.resume()
    }

    override fun applyMakeup(makeupTO: MakeupTO) {
        stateService.appliedMakeUpTypes[makeupTO.type] = makeupTO

        stateService.appliedMakeUpTypes.values.forEach {
            loadImage(it.ref, it.color)
        }
    }

    override fun removeMakeup(type: String) {
        val appliedMakeupTypes = stateService.appliedMakeUpTypes
        appliedMakeupTypes.remove(type)

        // Clear faceNode with makeup
        if (appliedMakeupTypes.isEmpty()) {
            stateService.clearFaceNodeSlot(MAKEUP_SLOT)
        } else {
            stateService.appliedMakeUpTypes.values.forEach {
                loadImage(it.ref, it.color)
            }
        }
    }

    override fun applyModel(modelTO: ModelTO) {
        stateService.addModel(modelTO)
        loadModel(modelTO)
    }

    override fun removeModel(slot: String) {
        stateService.clearFaceNodeSlot(slot)
    }

    override fun applyLook(lookTO: LookTO) {
        if (DrawHistoryHolder.isNotEmpty()) {
            showWarningDialog(lookTO)
        } else {
            maskEditorFragment.editorStateTO = lookTO.editorState

            stopAnimation()
            accessoriesOptionsFragment.applyState(lookTO.appliedModels)
            makeupOptionsFragment.applyState(lookTO.appliedMakeup)

            lookTO.appliedMakeup.forEach {
                stateService.appliedMakeUpTypes[it.type] = it
            }
            stateService.appliedMakeUpTypes.values.forEach {
                loadImage(it.ref, it.color)
            }

            lookTO.appliedModels.forEach {
                applyModel(it)
            }

            if (lookTO.isAnimated) {
                downloadLookFrames(lookTO.lookId)
            } else {
                downloadLookTextureAndApply(lookTO.lookId)
            }
        }
    }

    private fun showWarningDialog(lookTO: LookTO) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_apply_look, null)

        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

        dialogView.findViewById<Button>(R.id.cancel_popup_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.continue_button).setOnClickListener {
            stateService.clearAll()
            maskEditorFragment.clearAll()
            applyLook(lookTO)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun downloadLookTextureAndApply(lookId: String) {
        val ref =
            try {
                storage.getReference("$LOOKS_COLLECTION/$lookId/$MASK_FRAME_FILE_NAME")
            } catch (ex: Exception) {
                StyleableToast.makeText(this, ex.message, R.style.mytoast).show()
                return
            }

        ref.downloadUrl.addOnSuccessListener { uri ->
            Texture.builder()
                .setSource(applicationContext, uri)
                .build()
                .thenAccept { texture ->
                    stateService.applyTextureToFaceNode(texture, arSceneView, MASK_TEXTURE_SLOT)
                }
                .exceptionally { throwable ->
                    Log.println(
                        Log.ERROR,
                        null,
                        "Error creating texture: $throwable",
                    )
                    null
                }
        }.addOnFailureListener { ex ->
            StyleableToast.makeText(this, ex.message, R.style.mytoast).show()
        }
    }

    override fun removeLook(lookId: String) {
        stopAnimation()
        accessoriesOptionsFragment.resetMenu()
        makeupOptionsFragment.resetMenu()

        stateService.clearAll()
        maskEditorFragment.clearAll()
    }

    private fun onAttachFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment,
    ) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFrontFacingFragment
            arFragment.setOnViewCreatedListener { arSceneView: ArSceneView ->
                onViewCreated(
                    arSceneView,
                )
            }
        }
    }

    private fun onViewCreated(arSceneView: ArSceneView) {
        this.arSceneView = arSceneView
        arSceneView.lightEstimationConfig = LightEstimationConfig.DISABLED
        val light =
            LightManager.Builder(LightManager.Type.POINT)
                .position(-0.4f, 0.0f, -0.2f)
                .intensity(200_000.0f)
                .color(0.98f, 0.89f, 0.76f)
                .sunAngularRadius(1.9f)
                .sunHaloSize(10.0f)
                .sunHaloFalloff(80.0f)
                .castShadows(true)
                .build()
        arSceneView.environment?.indirectLight?.intensity = 5000f
        arSceneView.mainLight = light

        // This is important to make sure that the camera stream renders first so that
        // the face mesh occlusion works correctly.
        arSceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST)

        // Check for face detections

        arFragment.setOnAugmentedFaceUpdateListener { face ->
            onAugmentedFaceTrackingUpdate(face)
        }
    }

    private fun onAugmentedFaceTrackingUpdate(face: AugmentedFace) {
        stateService.hideNodesIfFaceTrackingStopped()

        if (stateService.faceNodesInfo.augmentedFace != face) {
            stateService.reapplyNodesForNewFace(face, arSceneView)
        }
    }

    private fun downloadLookFrames(lookId: String) {
        val ref =
            try {
                storage.getReference("$LOOKS_COLLECTION/$lookId/")
            } catch (e: Exception) {
                StyleableToast.makeText(this, e.message, R.style.mytoast).show()
                return
            }
        ref.listAll().addOnSuccessListener { listResult ->
            val amount = listResult.items.size
            repeat(amount) { index ->
                val fileRef =
                    storage.getReference("$LOOKS_COLLECTION/$lookId/${MASK_FRAME_FILE_NAME}_$index")
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    Texture.builder()
                        .setSource(applicationContext, uri)
                        .build()
                        .thenAccept { texture ->
                            gifTextures.add(texture)

                            if (index == amount - 1) {
                                startAnimation()
                            }
                        }
                        .exceptionally { throwable ->
                            Log.println(
                                Log.ERROR,
                                null,
                                "Error creating texture for frame $index: $throwable",
                            )
                            null
                        }
                }
            }
        }
    }

    private fun prepareAllGifTextures() {
        gifTextures.clear()
        val numberOfFrames = getNumberOfFrames(this)
        repeat(numberOfFrames) { index ->
            val frameBitmap =
                getNextTempMaskFrame(applicationContext, index)

            Texture.builder()
                .setSource(frameBitmap)
                .build()
                .thenAccept { texture ->
                    gifTextures.add(texture)
                    if (index == numberOfFrames - 1) {
                        startAnimation()
                    }
                }
                .exceptionally { throwable ->
                    Log.println(
                        Log.ERROR,
                        null,
                        "Error creating texture for frame $index: $throwable",
                    )
                    null
                }
        }
    }

    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        if (ArCoreApk.getInstance()
            .checkAvailability(this) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE
        ) {
            StyleableToast.makeText(this, "Augmented Faces requires ARCore", R.style.mytoast).show()
            finish()
            return false
        }
        val openGlVersionString =
            (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
                ?.deviceConfigurationInfo
                ?.glEsVersion

        openGlVersionString?.let {
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                StyleableToast.makeText(
                    this,
                    "Sceneform requires OpenGL ES 3.0 or later",
                    R.style.mytoast,
                ).show()
                finish()
                return false
            }
        }
        return true
    }

    private fun loadModel(modelTO: ModelTO) {
        storage.getReference(modelTO.modelRef)
            .downloadUrl
            .addOnSuccessListener { uri ->
                ModelRenderable.builder()
                    .setSource(this, uri)
                    .setIsFilamentGltf(true)
                    .build()
                    .thenAccept { renderable ->
                        stateService.applyModelOnFace(arSceneView, renderable, modelTO.slot)
                    }
                    .exceptionally { ex ->
                        Log.println(Log.ERROR, null, ex.message.orEmpty())
                        null
                    }
            }
            .addOnFailureListener { ex ->
                StyleableToast.makeText(applicationContext, ex.message, R.style.mytoast).show()
                Log.println(Log.ERROR, null, ex.message.orEmpty())
            }
    }

    private fun loadImage(
        ref: String,
        color: Int,
    ) {
        Glide.with(this)
            .asBitmap()
            .load(storage.getReference(ref))
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        stateService.loadImage(resource, color, arSceneView)
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                },
            )
    }

    private fun startAnimation() {
        if (gifRunnable == null) {
            gifRunnable =
                Runnable {
                    if (gifTextures.size > frameCounter) {
                        stateService.applyTextureToFaceNode(
                            gifTextures[frameCounter],
                            arSceneView,
                            MASK_TEXTURE_SLOT,
                        )
                        frameCounter = (frameCounter + 1) % gifTextures.size
                    }

                    gifRunnable?.let { handler.postDelayed(it, frameDelay) }
                }
        }
        gifRunnable?.let { handler.post(it) }
    }

    private fun stopAnimation() {
        gifTextures.clear()
        gifRunnable?.let { handler.removeCallbacks(it) }
        gifRunnable = null
    }

    private fun setSpeed(fps: Int) {
        frameDelay = 1000L / fps
    }

    private fun addMenuFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.menu_fragment_container, makeupOptionsFragment)
            .add(R.id.menu_fragment_container, accessoriesOptionsFragment)
            .add(R.id.menu_fragment_container, looksOptionsFragment)
            .commit()
    }

    private fun showMakeupOptionsMenu() {
        resetMenu()
        showFragment(makeupOptionsFragment)
        binding.makeupButton.setBackgroundResource(R.drawable.small_button)
    }

    private fun showAccessoriesMenu() {
        resetMenu()
        showFragment(accessoriesOptionsFragment)
        binding.accessoriesButton.setBackgroundResource(R.drawable.small_button)
    }

    private fun showLooksMenu() {
        resetMenu()
        showFragment(looksOptionsFragment)
        looksOptionsFragment.fetchLooks()
        binding.looksButton.setBackgroundResource(R.drawable.small_button)
    }

    private fun showCameraModeUI() {
        findViewById<View>(R.id.top_ui).visibility = View.GONE
        findViewById<View>(R.id.bottom_ui).visibility = View.GONE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, cameraModeFragment)
            .commit()
        cameraModeFragment.setARFragment(arFragment)
    }

    private fun showProfileUI() {
        findViewById<View>(R.id.top_ui).visibility = View.GONE
        findViewById<View>(R.id.bottom_ui).visibility = View.GONE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, profileFragment)
            .commit()
    }

    private fun showMakeupEditorUI() {
        findViewById<View>(R.id.top_ui).visibility = View.GONE
        findViewById<View>(R.id.bottom_ui).visibility = View.GONE
        val fragmentManager = supportFragmentManager
        val existingFragment =
            fragmentManager.findFragmentByTag(MaskEditorFragment.MAKEUP_EDITOR_FRAGMENT_TAG)

        if (existingFragment == null) {
            fragmentManager.beginTransaction()
                .add(
                    R.id.makeup_editor_fragment_container,
                    maskEditorFragment,
                    MaskEditorFragment.MAKEUP_EDITOR_FRAGMENT_TAG,
                )
                .commit()
        } else {
            showFragment(maskEditorFragment)
        }

        arSceneView.pause()

        maskEditorFragment.applyBackgroundBitmap(stateService.makeupTextureBitmap)
    }

    private fun resetMenu() {
        for (i in 0 until binding.menuButtons.childCount) {
            val child =
                binding.menuButtons.getChildAt(i).apply {
                    background = null
                }
        }

        hideMenuFragments()
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .show(fragment)
            .commit()
    }

    private fun hideMenuFragments() {
        supportFragmentManager.beginTransaction()
            .hide(makeupOptionsFragment)
            .hide(accessoriesOptionsFragment)
            .hide(looksOptionsFragment)
            .commit()
    }

    private fun saveLook(
        isPublic: Boolean,
        name: String,
    ) {
        val lookId = UUID.randomUUID().toString()
        val isAnimated = doesTempAnimatedMaskExist(applicationContext)

        if (isAnimated) {
            saveFrames(lookId)
        } else {
            saveMaskTexture(lookId)
        }

        val data =
            LookTO(
                isPublic = isPublic,
                lookId = lookId,
                author = auth.currentUserUsername(),
                isAnimated = isAnimated,
                appliedMakeup = stateService.getAppliedMakeupList(),
                appliedModels = stateService.getAppliedModelsList(),
                editorState = maskEditorFragment.serializeEditorState(),
                name = name,
                imagePreviewRef = createPreview(lookId),
            )

        fireStore.collection(LOOKS_COLLECTION)
            .document(lookId)
            .set(data)
            .addOnSuccessListener {
                Log.println(
                    Log.INFO,
                    null,
                    "Look $lookId uploaded",
                )
                StyleableToast.makeText(
                    applicationContext,
                    "Look saved successfully",
                    R.style.mytoast,
                ).show()

                looksOptionsFragment.fetchLooks()
            }
            .addOnFailureListener { ex -> Log.println(Log.ERROR, null, "onFailure: $ex") }
    }

    /**
     * Create preview image for look, if no makeup or texture were added to the look,
     * look name will be used as preview
     *
     * @return reference to storage for preview image or empty string if there is no preview
     */
    private fun createPreview(lookId: String): String {
        val isAnimated = doesTempAnimatedMaskExist(applicationContext)

        val firstFrameBitmap =
            if (isAnimated) {
                getNextTempMaskFrame(this, 0)
            } else {
                getTempMaskTextureBitmap(this)
            }

        if (firstFrameBitmap == null && stateService.makeupTextureBitmap == null) {
            return ""
        }

        val combinedBitmap =
            combineBitmaps(
                listOfNotNull(stateService.makeupTextureBitmap, firstFrameBitmap),
                PREVIEW_BITMAP_SIZE,
                PREVIEW_BITMAP_SIZE,
            )

        val ref =
            storage.getReference("$PREVIEW_COLLECTION/$lookId")
        val stream = ByteArrayOutputStream()
        combinedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val uploadTask =
            ref.putStream(ByteArrayInputStream(stream.toByteArray()))

        uploadTask.addOnFailureListener {
            StyleableToast.makeText(applicationContext, it.message, R.style.mytoast).show()
        }

        return "$PREVIEW_COLLECTION/$lookId"
    }

    private fun saveFrames(lookId: String) {
        var counter = 0

        while (true) {
            val frameStream =
                getNextTempMaskFrameInputStream(applicationContext, counter) ?: break
            val ref =
                storage.getReference("$LOOKS_COLLECTION/$lookId/${MASK_FRAME_FILE_NAME}_$counter")

            val uploadTask =
                ref.putStream(frameStream)

            uploadTask.addOnFailureListener {
                StyleableToast.makeText(applicationContext, it.message, R.style.mytoast).show()
            }

            counter++
        }
    }

    private fun saveMaskTexture(lookId: String) {
        val frameStream = getTempMaskTextureStream(applicationContext)
        frameStream?.let {
            val ref =
                storage.getReference("$LOOKS_COLLECTION/$lookId/$MASK_FRAME_FILE_NAME")

            val uploadTask =
                ref.putStream(it)

            uploadTask.addOnFailureListener { ex ->
                StyleableToast.makeText(applicationContext, ex.message, R.style.mytoast).show()
            }
        }
    }

    private fun clearAll() {
        stopAnimation()
        deleteTempFiles(applicationContext)

        accessoriesOptionsFragment.resetMenu()
        makeupOptionsFragment.resetMenu()
        looksOptionsFragment.resetMenu()

        stateService.clearAll()
        maskEditorFragment.clearAll()
    }

    override fun showMainLayout() {
        findViewById<View>(R.id.top_ui).visibility = View.VISIBLE
        findViewById<View>(R.id.bottom_ui).visibility = View.VISIBLE

        val makeupEditor =
            supportFragmentManager.findFragmentByTag(MaskEditorFragment.MAKEUP_EDITOR_FRAGMENT_TAG)

        val transaction =
            supportFragmentManager
                .beginTransaction()
                .remove(cameraModeFragment)
                .remove(profileFragment)

        makeupEditor?.let { transaction.hide(makeupEditor) }
        transaction.commit()

        arSceneView.resume()

        val bitmap = getTempMaskTextureBitmap(applicationContext)
        bitmap?.let {
            stateService.createTextureAndApply(it, arFragment.arSceneView, MASK_TEXTURE_SLOT)
        }

        if (doesTempAnimatedMaskExist(applicationContext)) {
            prepareAllGifTextures()
        }
    }

    private fun showSaveLookDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_save_look, null)
        val checkbox = dialogView.findViewById<CheckBox>(R.id.is_public_checkbox)
        val editText = dialogView.findViewById<EditText>(R.id.look_name_input)

        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

        dialogView.findViewById<Button>(R.id.cancel_popup_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.save_look_button).setOnClickListener {
            if (editText.text.isEmpty()) {
                editText.error = "Name should contain at least 1 symbol"
            } else if (editText.text.toString().length > MAX_LOOK_NAME_LENGTH) {
                editText.error = "Name should contain no more than $MAX_LOOK_NAME_LENGTH symbols"
            } else {
                saveLook(checkbox.isChecked, editText.text.toString())
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }
}
