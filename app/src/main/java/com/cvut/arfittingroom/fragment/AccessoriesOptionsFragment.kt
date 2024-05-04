package com.cvut.arfittingroom.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.ResourceListener
import com.cvut.arfittingroom.model.ACCESSORY_TYPES_COLLECTION
import com.cvut.arfittingroom.model.MODELS_COLLECTION
import com.cvut.arfittingroom.model.ModelInfo
import com.cvut.arfittingroom.model.PREVIEW_IMAGE_ATTRIBUTE
import com.cvut.arfittingroom.model.REF_ATTRIBUTE
import com.cvut.arfittingroom.model.SLOT_ATTRIBUTE
import com.cvut.arfittingroom.model.TYPE_ATTRIBUTE
import com.cvut.arfittingroom.module.GlideApp
import com.cvut.arfittingroom.utils.ScreenUtil.dpToPx
import com.cvut.arfittingroom.utils.UIUtil.createDivider
import com.cvut.arfittingroom.utils.UIUtil.deselectButton
import com.cvut.arfittingroom.utils.UIUtil.selectSquareButton
import com.cvut.arfittingroom.utils.makeFirstLetterCapital
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AccessoriesOptionsFragment : Fragment() {
    private val accessoriesTypes = mutableSetOf<String>()
    private val modelsInfo = mutableListOf<ModelInfo>()
    private val selectedOptionTypeToViewId = mutableMapOf<String, Int>()
    private var selectedAccessoryType: String = ""
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_menu_vertical_scroll, container, false)

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
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateAccessoriesTypesMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.options)

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
        firestore.collection(MODELS_COLLECTION)
            .whereEqualTo(TYPE_ATTRIBUTE, type)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val ref = document[REF_ATTRIBUTE].toString()
                    val preview = document[PREVIEW_IMAGE_ATTRIBUTE].toString()
                    val slot = document[SLOT_ATTRIBUTE].toString().takeIf { it.isNotEmpty() } ?: ref

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
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateModelsOptionsMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.options)
        options.removeAllViews()

        // Add accessory type button
        val button =
            Button(context).apply {
                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).also {
                        it.setMargins(0, 0, dpToPx(10, context), 0)
                    }
                id = selectedAccessoryType.hashCode()
                text = selectedAccessoryType.makeFirstLetterCapital()
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                isAllCaps = false
                background = null
                setOnClickListener {
                    // Return back
                    fetchAccessoriesTypes(view)
                }
            }

        options.addView(button)
        options.addView(createDivider(requireContext()))

        modelsInfo.forEach { modelInfo ->
            val imageView =
                ImageView(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            dpToPx(100, context),
                            dpToPx(100, context),
                        )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    background = null
                    id = modelInfo.modelRef.hashCode()
                    setOnClickListener {
                        selectAccessoriesOption(view, this, modelInfo)
                    }
                }

            options.addView(imageView)

            if (selectedOptionTypeToViewId[selectedAccessoryType] == imageView.id) {
                selectSquareButton(imageView)
            }

            GlideApp.with(this)
                .load(storage.getReference(modelInfo.imagePreviewRef))
                .thumbnail()
                .into(imageView)
        }
    }

    private fun selectAccessoriesOption(
        view: View,
        imageView: ImageView,
        modelInfo: ModelInfo,
    ) {
        selectedOptionTypeToViewId[modelInfo.type]?.let { viewId ->
            view.findViewById<ImageView>(viewId)?.let { deselectButton(it) }
        }

        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        if (selectedOptionTypeToViewId[modelInfo.type] == imageView.id) {
            selectedOptionTypeToViewId.remove(modelInfo.type)
            listener.removeModel(modelInfo.slot)

            selectedOptionTypeToViewId.remove(modelInfo.type)
        } else {
            listener.applyModel(
                modelInfo,
            )

            selectSquareButton(imageView)
            selectedOptionTypeToViewId[modelInfo.type] = imageView.id
        }
    }

}
