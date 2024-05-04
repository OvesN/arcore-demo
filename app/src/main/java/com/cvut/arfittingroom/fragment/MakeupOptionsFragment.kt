package com.cvut.arfittingroom.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.ResourceListener
import com.cvut.arfittingroom.model.COLORS_COLLECTION
import com.cvut.arfittingroom.model.DEFAULT_COLOR_ATTRIBUTE
import com.cvut.arfittingroom.model.MAKEUP_COLLECTION
import com.cvut.arfittingroom.model.MAKEUP_TYPES_COLLECTION
import com.cvut.arfittingroom.model.MakeupType
import com.cvut.arfittingroom.model.REF_ATTRIBUTE
import com.cvut.arfittingroom.model.TYPE_ATTRIBUTE
import com.cvut.arfittingroom.module.GlideApp
import com.cvut.arfittingroom.utils.ScreenUtil.dpToPx
import com.cvut.arfittingroom.utils.makeFirstLetterCapital
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class MakeupOptionsFragment : Fragment() {
    private val makeupTypes = mutableListOf<MakeupType>()
    private var selectedMakeupOptionRef: String = ""
    private var selectedColor: Int = Color.WHITE
    private var selectedMakeupType: String = ""
    private val selectedOptionTypeToViewId = mutableMapOf<String, Int>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // TODO REFACTOR
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_menu_horizontal_scroll, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { fetchMakeupTypes(it) }
    }

    private fun fetchMakeupTypes(view: View) {
        firestore.collection(MAKEUP_TYPES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                makeupTypes.clear()
                for (document in result) {
                    makeupTypes.add(
                        MakeupType(
                            document.id.makeFirstLetterCapital(),
                            Color.parseColor(document.get(DEFAULT_COLOR_ATTRIBUTE).toString()),
                        ),
                    )
                }

                if (isAdded) {
                    updateMakeupTypesMenu(view)
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateMakeupTypesMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.options)
        options.removeAllViews()

        for (makeupType in makeupTypes) {
            val button =
                Button(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    text = makeupType.type
                    isAllCaps = false
                    background = null
                    id = View.generateViewId()
                    setOnClickListener {
                        fetchMakeupOptions(makeupType.type.lowercase())
                        selectedColor = makeupType.defaultColor
                    }
                }
            options.addView(button)

            if (makeupType != makeupTypes.last()) {
                options.addView(createDivider())
            }
        }
    }

    private fun fetchMakeupOptions(type: String) {
        firestore.collection(MAKEUP_COLLECTION)
            .whereEqualTo(TYPE_ATTRIBUTE, type)
            .get()
            .addOnSuccessListener { result ->
                val imageRefs =
                    result.map { document ->
                        document.getString(REF_ATTRIBUTE)!!
                    }
                view?.let {
                    updateMakeupOptionsMenu(it, imageRefs, type)
                }

                selectedMakeupType = type
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchColorOptions() {
        firestore.collection(COLORS_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                view?.let { view ->
                    updateColorOptionsMenu(view, result.map { Color.parseColor(it.id) })
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateColorOptionsMenu(
        view: View,
        colors: List<Int>,
    ) {
        val options = view.findViewById<LinearLayout>(R.id.options)
        options.removeAllViews()

        // Add makeup option button
        val imageView =
            ImageView(context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        dpToPx(100, context),
                        dpToPx(100, context),
                    )
                scaleType = ImageView.ScaleType.FIT_CENTER
                background = ContextCompat.getDrawable(context, R.drawable.head_model)
                id = selectedMakeupOptionRef.hashCode()
                setOnClickListener {
                    // Return back
                    fetchMakeupOptions(selectedMakeupType)
                }
            }

        GlideApp.with(this)
            .load(storage.getReference(selectedMakeupOptionRef))
            .thumbnail()
            .into(imageView)

        options.addView(imageView)
        options.addView(createDivider())

        // Add color options
        colors.forEach { color ->
            val colorImageView =
                ImageView(context).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(
                            dpToPx(40, context),
                            dpToPx(40, context),
                        ).also {
                            it.setMargins(dpToPx(10, context), 0, 0, 0)
                        }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    background = createColorOptionImage(color)
                    id = color
                    setOnClickListener {
                        selectColor(this, view, color)
                        applyImage(selectedMakeupOptionRef, selectedMakeupType)
                    }
                }
            options.addView(colorImageView)
        }

        // Add color picker
        val colorPickerImageView =
            ImageView(context).apply {
                layoutParams =
                    LinearLayout.LayoutParams(
                        dpToPx(40, context),
                        dpToPx(40, context),
                    ).also {
                        it.setMargins(dpToPx(10, context), 0, 0, 0)
                    }
                scaleType = ImageView.ScaleType.FIT_CENTER
                background = ContextCompat.getDrawable(requireContext(), R.drawable.color_picker)!!
                setOnClickListener {
                    showColorPickerDialog()
                }
            }

        options.addView(colorPickerImageView)
    }

    private fun selectColor(
        imageView: ImageView,
        view: View,
        color: Int,
    ) {
        selectedOptionTypeToViewId["color"]?.let {
            val selectedView =
                view.findViewById<ImageView>(it).apply {
                    background = createColorOptionImage(it)
                }
        }

        selectedColor = color

        val layers =
            arrayOf(
                createColorOptionImage(color),
                ContextCompat.getDrawable(requireContext(), R.drawable.circle_border)!!,
            )

        imageView.background = LayerDrawable(layers)

        selectedOptionTypeToViewId["color"] = imageView.id
    }

    private fun createColorOptionImage(color: Int): Drawable {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.circle)!!.mutate()
        val wrap = DrawableCompat.wrap(icon)
        DrawableCompat.setTint(wrap, color)

        return wrap
    }

    private fun updateMakeupOptionsMenu(
        view: View,
        imageRefs: List<String>,
        type: String,
    ) {
        val options = view.findViewById<LinearLayout>(R.id.options)
        options.removeAllViews()

        val button =
            Button(context).apply {
                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).also {
                        it.setMargins(0, 0, dpToPx(10, context), 0)
                    }
                text = type.makeFirstLetterCapital()
                textAlignment = TEXT_ALIGNMENT_CENTER
                isAllCaps = false
                background = null
                id = type.hashCode()
                setOnClickListener {
                    fetchMakeupTypes(view)
                }
            }

        options.addView(button)
        options.addView(createDivider())

        imageRefs.forEach { ref ->
            val imageView =
                ImageView(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            dpToPx(100, context),
                            dpToPx(100, context),
                        )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    background = ContextCompat.getDrawable(context, R.drawable.head_model)
                    id = ref.hashCode()
                    setOnClickListener {
                        selectMakeupOption(view, this, type = type, ref = ref)
                        fetchColorOptions()
                    }
                }

            options.addView(imageView)

            GlideApp.with(this)
                .load(storage.getReference(ref))
                .thumbnail()
                .into(imageView)
        }
    }

    private fun selectMakeupOption(
        view: View,
        imageView: ImageView,
        ref: String,
        type: String,
    ) {
        selectedMakeupOptionRef = ref

        val selectedViewId = selectedOptionTypeToViewId[type]

        selectedViewId?.let {
            view.findViewById<ImageView>(it)?.background =
                ContextCompat.getDrawable(imageView.context, R.drawable.head_model)!!
        }

        val layers =
            arrayOf(
                ContextCompat.getDrawable(imageView.context, R.drawable.head_model)!!,
                ContextCompat.getDrawable(imageView.context, R.drawable.border)!!,
            )

        imageView.background = LayerDrawable(layers)

        applyImage(ref, type, shouldBeReplaced = (selectedOptionTypeToViewId[type] == imageView.id))
        selectedOptionTypeToViewId[type] = imageView.id
    }

    private fun applyImage(
        ref: String,
        type: String,
        shouldBeReplaced: Boolean = false,
    ) {
        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        if (shouldBeReplaced) {
            selectedOptionTypeToViewId.remove(type)

            listener.removeImage(type)
        } else {
            listener.applyImage(type, ref, selectedColor)
        }
    }

    private fun createDivider() =
        View(context).apply {
            layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
            background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
        }

    // TODO SEPARATE
    private fun showColorPickerDialog() {
        context?.let { ctx ->
            ColorPickerDialog.Builder(ctx)
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(
                    R.string.OK,
                    ColorEnvelopeListener { envelope, _ ->
                        selectedColor = envelope.color
                        applyImage(selectedMakeupOptionRef, selectedMakeupType)
                    },
                )
                .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show()
        } ?: Log.e("ColorPickerDialog", "Context is null while trying to show color picker dialog.")
    }
}
