package com.cvut.arfittingroom.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
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
import com.cvut.arfittingroom.model.to.MakeupTO
import com.cvut.arfittingroom.module.GlideApp
import com.cvut.arfittingroom.utils.ScreenUtil.dpToPx
import com.cvut.arfittingroom.utils.UIUtil.createColorOptionImage
import com.cvut.arfittingroom.utils.UIUtil.createDivider
import com.cvut.arfittingroom.utils.UIUtil.deselectColorButton
import com.cvut.arfittingroom.utils.UIUtil.deselectHeadBackgroundButton
import com.cvut.arfittingroom.utils.UIUtil.selectColorButton
import com.cvut.arfittingroom.utils.UIUtil.selectHeadBackgroundButton
import com.cvut.arfittingroom.utils.UIUtil.showColorPickerDialog
import com.cvut.arfittingroom.utils.makeFirstLetterCapital
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.github.muddz.styleabletoast.StyleableToast

/**
 * Displays makeup menu, allows users to select and apply specific makeup
 *
 * @author Veronika Ovsyannikova
 */
class MakeupMenuFragment : Fragment() {
    private val makeupTypes = mutableListOf<MakeupType>()
    private var selectedMakeupOptionRef: String = ""
    private var selectedColor: Int = Color.WHITE
    private var selectedMakeupType: String = ""
    private val selectedOptionTypeToViewId = mutableMapOf<String, Int>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { fetchMakeupTypes() }
    }

    fun fetchMakeupTypes() {
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
                    updateMakeupTypesMenu(requireView())
                }
            }
            .addOnFailureListener { ex ->
                if (isAdded) {
                    StyleableToast.makeText(requireContext(), ex.message, R.style.mytoast).show()
                }
            }
    }

    private fun updateMakeupTypesMenu(view: View) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
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
                options.addView(createDivider(requireContext()))
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
            .addOnFailureListener { ex ->
                if (isAdded) {
                    StyleableToast.makeText(requireContext(), ex.message, R.style.mytoast).show()
                }
            }
    }

    private fun fetchColorOptions() {
        firestore.collection(COLORS_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                view?.let { view ->
                    updateColorOptionsMenu(view, result.map { Color.parseColor(it["color"].toString()) })
                }
            }
            .addOnFailureListener { ex ->
                if (isAdded) {
                    StyleableToast.makeText(requireContext(), ex.message, R.style.mytoast).show()
                }
            }
    }

    private fun updateColorOptionsMenu(
        view: View,
        colors: List<Int>,
    ) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
        options.removeAllViews()
        selectedOptionTypeToViewId["color"] = 0

        // Add makeup option button
        val imageButton =
            ImageButton(context).apply {
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
            .into(imageButton)

        options.addView(imageButton)
        options.addView(createDivider(requireContext()))

        // Add color options
        colors.forEach { color ->
            val colorImageButton =
                ImageButton(context).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(
                            dpToPx(40, context),
                            dpToPx(40, context),
                        ).also {
                            it.setMargins(dpToPx(10, context), 0, 0, 0)
                        }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    background = createColorOptionImage(requireContext(), color)
                    id = color
                    setOnClickListener {
                        selectColor(this, view, color)
                        toggleMakeup(selectedMakeupOptionRef, selectedMakeupType)
                    }
                }

            options.addView(colorImageButton)
        }

        // Add color picker
        val colorPickerImageButton =
            ImageButton(context).apply {
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
                    showColorPickerDialog(requireContext(), selectedColor) { envelopColor, _ ->
                        selectedColor = envelopColor
                        toggleMakeup(selectedMakeupOptionRef, selectedMakeupType)
                    }
                }
            }

        options.addView(colorPickerImageButton)
    }

    private fun selectColor(
        imageView: ImageView,
        view: View,
        color: Int,
    ) {
        selectedOptionTypeToViewId["color"]?.let { viewId ->
            view.findViewById<ImageView>(viewId)?.let { deselectColorButton(it) }
        }

        selectedColor = color

        selectColorButton(imageView)

        selectedOptionTypeToViewId["color"] = imageView.id
    }

    private fun updateMakeupOptionsMenu(
        view: View,
        imageRefs: List<String>,
        type: String,
    ) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
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
                    fetchMakeupTypes()
                }
            }

        options.addView(button)
        options.addView(createDivider(requireContext()))

        imageRefs.forEach { ref ->
            val imageButton =
                ImageButton(context).apply {
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
                    }
                }

            options.addView(imageButton)

            if (selectedOptionTypeToViewId[selectedMakeupType] == imageButton.id) {
                selectHeadBackgroundButton(imageButton)
            }

            GlideApp.with(this)
                .load(storage.getReference(ref))
                .thumbnail()
                .into(imageButton)
        }
    }

    private fun selectMakeupOption(
        view: View,
        imageView: ImageView,
        ref: String,
        type: String,
    ) {
        selectedMakeupOptionRef = ref

        selectedOptionTypeToViewId[type]?.let { viewId ->
            view.findViewById<ImageView>(viewId)?.let { deselectHeadBackgroundButton(it) }
        }

        val shouldRemove = selectedOptionTypeToViewId[type] == imageView.id

        toggleMakeup(ref, type, shouldRemove)

        if (shouldRemove) {
            selectedOptionTypeToViewId.remove(type)
        } else {
            selectedOptionTypeToViewId[type] = imageView.id
            selectHeadBackgroundButton(imageView)
            fetchColorOptions()
        }
    }

    private fun toggleMakeup(
        ref: String,
        type: String,
        shouldRemove: Boolean = false,
    ) {
        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        if (shouldRemove) {
            selectedOptionTypeToViewId.remove(type)
            listener.removeMakeup(type)
        } else {
            listener.applyMakeup(MakeupTO(ref = ref, type = type, color = selectedColor))
        }
    }

    fun applyState(selectedMakeup: List<MakeupTO>) {
        selectedOptionTypeToViewId.clear()

        selectedMakeup.forEach {
            selectedOptionTypeToViewId[it.type] = it.ref.hashCode()
        }
    }

    fun resetMenu() {
        selectedOptionTypeToViewId.clear()
        fetchMakeupTypes()
    }
}
