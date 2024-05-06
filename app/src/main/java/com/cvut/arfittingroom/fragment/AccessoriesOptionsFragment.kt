package com.cvut.arfittingroom.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.ResourceListener
import com.cvut.arfittingroom.model.ACCESSORY_TYPES_COLLECTION
import com.cvut.arfittingroom.model.MODELS_COLLECTION
import com.cvut.arfittingroom.model.ModelInfo
import com.cvut.arfittingroom.model.NUM_OF_ELEMENTS_IN_ROW
import com.cvut.arfittingroom.model.PREVIEW_IMAGE_ATTRIBUTE
import com.cvut.arfittingroom.model.REF_ATTRIBUTE
import com.cvut.arfittingroom.model.SLOT_ATTRIBUTE
import com.cvut.arfittingroom.model.TYPE_ATTRIBUTE
import com.cvut.arfittingroom.module.GlideApp
import com.cvut.arfittingroom.utils.ScreenUtil.dpToPx
import com.cvut.arfittingroom.utils.UIUtil.deselectButton
import com.cvut.arfittingroom.utils.UIUtil.selectSquareButton
import com.cvut.arfittingroom.utils.makeFirstLetterCapital
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.github.muddz.styleabletoast.StyleableToast

class AccessoriesOptionsFragment : Fragment() {
    private val accessoriesTypes = mutableSetOf<String>()
    private val modelsInfo = mutableListOf<ModelInfo>()
    private val selectedSlotToViewId = mutableMapOf<String, Int>()
    private var selectedAccessoryType: String = ""
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { fetchAccessoriesTypes(it) }
    }

    private fun fetchAccessoriesTypes(view: View) {
        view.findViewById<ConstraintLayout>(R.id.vertical_scroll_view).visibility = View.GONE

        firestore.collection(ACCESSORY_TYPES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                accessoriesTypes.clear()
                for (document in result) {
                    accessoriesTypes.add(document.id.makeFirstLetterCapital())
                }

                if (isAdded) {
                    updateAccessoriesTypesMenu(view)
                }
            }
            .addOnFailureListener { ex ->
                if (isAdded) {
                    StyleableToast.makeText(requireContext(), ex.message, R.style.mytoast).show()
                }
            }
    }

    private fun updateAccessoriesTypesMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
        options.removeAllViews()
        view.findViewById<GridLayout>(R.id.vertical_options).removeAllViews()

        for (type in accessoriesTypes) {
            val button =
                Button(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    text = type
                    isAllCaps = false
                    background = null
                    id = View.generateViewId()
                    setOnClickListener {
                        selectedAccessoryType = type
                        fetchModelsOptions(type.lowercase())
                    }
                }
            options.addView(button)

            if (type != accessoriesTypes.last()) {
                val divider =
                    View(context).apply {
                        layoutParams =
                            ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
                        background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
                    }

                options.addView(divider)
            }
        }
    }

    private fun fetchModelsOptions(type: String) {
        modelsInfo.clear()
        firestore.collection(MODELS_COLLECTION)
            .whereEqualTo(TYPE_ATTRIBUTE, type)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val ref = document[REF_ATTRIBUTE].toString()
                    val preview = document[PREVIEW_IMAGE_ATTRIBUTE].toString()
                    val slot = document[SLOT_ATTRIBUTE]?.toString() ?: ref

                    modelsInfo.add(
                        ModelInfo(
                            slot = slot,
                            modelRef = ref,
                            imagePreviewRef = preview,
                            type = type,
                        ),
                    )
                }
                view?.let { updateModelsOptionsMenu(it) }
            }
            .addOnFailureListener { ex ->
                if (isAdded) {
                    StyleableToast.makeText(requireContext(), ex.message, R.style.mytoast).show()
                }
            }
    }

    private fun updateModelsOptionsMenu(view: View) {
        view.findViewById<LinearLayout>(R.id.horizontal_options).removeAllViews()
        view.findViewById<ConstraintLayout>(R.id.vertical_scroll_view).visibility = View.VISIBLE

        val options = view.findViewById<GridLayout>(R.id.vertical_options)
        options.removeAllViews()

        val typeButton = view.findViewById<Button>(R.id.type_button).apply {
            text =
                selectedAccessoryType.makeFirstLetterCapital().chunked(10).joinToString("\n")
        }
        typeButton.setOnClickListener {
            // Return back
            fetchAccessoriesTypes(view)
        }

        options.post {
            val imageWidth =
                (options.width - options.paddingStart - options.paddingEnd) / NUM_OF_ELEMENTS_IN_ROW

            modelsInfo.forEach { modelInfo ->
                val imageButton =
                    ImageButton(context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        background = null
                        id = modelInfo.modelRef.hashCode()
                        setOnClickListener {
                            selectAccessoriesOption(view, this, modelInfo)
                        }
                    }

                val params = GridLayout.LayoutParams().apply {
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    height = dpToPx(100, requireContext())
                    width = imageWidth
                }

                params.setGravity(Gravity.START)

                options.addView(imageButton, params)

                if (selectedSlotToViewId.values.contains(imageButton.id)) {
                    selectSquareButton(imageButton)
                }

                GlideApp.with(this)
                    .load(storage.getReference(modelInfo.imagePreviewRef))
                    .thumbnail()
                    .into(imageButton)
            }
        }
    }

    private fun selectAccessoriesOption(
        view: View,
        imageView: ImageView,
        modelInfo: ModelInfo,
    ) {
        selectedSlotToViewId[modelInfo.slot]?.let { viewId ->
            view.findViewById<ImageView>(viewId)?.let { deselectButton(it) }
        }

        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        if (selectedSlotToViewId[modelInfo.slot] == imageView.id) {
            selectedSlotToViewId.remove(modelInfo.slot)
            listener.removeModel(modelInfo.slot)

            selectedSlotToViewId.remove(modelInfo.slot)
        } else {
            listener.applyModel(
                modelInfo,
            )

            selectSquareButton(imageView)
            selectedSlotToViewId[modelInfo.slot] = imageView.id
        }
    }
}
