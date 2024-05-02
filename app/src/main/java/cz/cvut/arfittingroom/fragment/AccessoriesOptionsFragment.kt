package cz.cvut.arfittingroom.fragment

import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.activity.ResourceListener
import cz.cvut.arfittingroom.model.ACCESSORY_TYPES_COLLECTION
import cz.cvut.arfittingroom.model.DEFAULT_COLOR_ATTRIBUTE
import cz.cvut.arfittingroom.model.MAKEUP_TYPES_COLLECTION
import cz.cvut.arfittingroom.model.MODELS_COLLECTION
import cz.cvut.arfittingroom.model.MakeupType
import cz.cvut.arfittingroom.model.ModelInfo
import cz.cvut.arfittingroom.model.PREVIEW_IMAGE_ATTRIBUTE
import cz.cvut.arfittingroom.model.REF_ATTRIBUTE
import cz.cvut.arfittingroom.model.TYPE_ATTRIBUTE
import cz.cvut.arfittingroom.model.enums.ENodeType
import cz.cvut.arfittingroom.module.GlideApp
import cz.cvut.arfittingroom.utils.ScreenUtil.dpToPx
import cz.cvut.arfittingroom.utils.makeFirstLetterCapital

class AccessoriesOptionsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_vertical_scroll, container, false)
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val accessoriesTypes = mutableSetOf<String>()
    private val modelsInfo = mutableListOf<ModelInfo>()

    private val selectedOptionTypeToViewId = mutableMapOf<String, Int>()
    private var selectedAccessoryType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { fetchAccessoriesTypes(it) }
    }

    private fun fetchAccessoriesTypes(view: View) {
        firestore.collection(ACCESSORY_TYPES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                accessoriesTypes.clear()
                for (document in result) {
                    val docId = document.id
                    val capitalizedDocId = docId.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }
                    accessoriesTypes.add(capitalizedDocId)
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
            val button = Button(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
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
                val divider = View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
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

                    //FIXME load from db
                    val nodeType =
                        if (type == "glasses") ENodeType.EYES else if (type == "hats") ENodeType.TOP_HEAD else ENodeType.MAKEUP

                    modelsInfo.add(
                        ModelInfo(
                            nodeType,
                            modelRef = ref,
                            imagePreviewRef = preview,
                            type = type
                        )
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
        val button = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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
        options.addView(createDivider())

        modelsInfo.forEach { modelInfo ->
            val imageView = ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    dpToPx(100, context),
                    dpToPx(100, context)
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                background = null
                id = modelInfo.modelRef.hashCode()
                setOnClickListener {
                    selectAccessoriesOption(view, this, modelInfo)
                }
            }

            options.addView(imageView)

            GlideApp.with(this)
                .load(storage.getReference(modelInfo.imagePreviewRef))
                .thumbnail()
                .into(imageView)
        }
    }


    private fun selectAccessoriesOption(
        view: View,
        imageView: ImageView,
        modelInfo: ModelInfo
    ) {
        val selectedViewId = selectedOptionTypeToViewId[modelInfo.type]

        selectedViewId?.let {
            view.findViewById<ImageView>(it)?.background =
                null
        }

        imageView.background = ContextCompat.getDrawable(imageView.context, R.drawable.border)!!

        applyModel(
            modelInfo,
            shouldBeReplaced = (selectedOptionTypeToViewId[modelInfo.type] == imageView.id)
        )
        selectedOptionTypeToViewId[modelInfo.type] = imageView.id
    }

    private fun applyModel(modelInfo: ModelInfo, shouldBeReplaced: Boolean = false) {
        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        if (shouldBeReplaced) {
            selectedOptionTypeToViewId.remove(modelInfo.type)
            listener.removeModel(modelInfo.type)
        } else {
            listener.applyModel(
                modelInfo
            )
        }
    }

    private fun createDivider() =
        View(context).apply {
            layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
            background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
        }
}