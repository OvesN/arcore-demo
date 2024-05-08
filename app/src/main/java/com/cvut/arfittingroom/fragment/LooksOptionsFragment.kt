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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.ResourceListener
import com.cvut.arfittingroom.model.AUTHOR_ATTRIBUTE
import com.cvut.arfittingroom.model.IS_PUBLIC_ATTRIBUTE
import com.cvut.arfittingroom.model.LOOKS_COLLECTION
import com.cvut.arfittingroom.model.LookInfo
import com.cvut.arfittingroom.model.MakeupInfo
import com.cvut.arfittingroom.model.ModelInfo
import com.cvut.arfittingroom.model.NUM_OF_ELEMENTS_IN_ROW
import com.cvut.arfittingroom.model.NUM_OF_ELEMENTS_IN_ROW_SMALL_MENU
import com.cvut.arfittingroom.model.PREVIEW_IMAGE_ATTRIBUTE
import com.cvut.arfittingroom.module.GlideApp
import com.cvut.arfittingroom.utils.DeserializationUtil.deserializeFromMap
import com.cvut.arfittingroom.utils.ScreenUtil
import com.cvut.arfittingroom.utils.UIUtil.deselectButton
import com.cvut.arfittingroom.utils.UIUtil.selectSquareButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.TreeMap

class LooksOptionsFragment : Fragment() {
    private var selectedLookViewId: Int = 0
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val looks = mutableMapOf<String, LookInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.divider).visibility = View.GONE
        view.findViewById<View>(R.id.type_button).visibility = View.GONE
        view.findViewById<View>(R.id.vertical_scroll_view).visibility = View.VISIBLE

        view.findViewById<GridLayout>(R.id.vertical_options).columnCount = NUM_OF_ELEMENTS_IN_ROW
    }

    fun fetchLooks() {
        looks.clear()

        firestore.collection(LOOKS_COLLECTION)
            .where(
                Filter.or(
                    Filter.equalTo(IS_PUBLIC_ATTRIBUTE, true),
                    Filter.equalTo(
                        AUTHOR_ATTRIBUTE,
                        auth.currentUser?.let { it.email?.substringBefore("@") })
                )
            )
            .get().addOnSuccessListener { result ->
                result.documents.forEach { look ->
                    val data = look.data
                    val lookInfo = data?.let { deserializeFromMap(it, LookInfo::class.java) }
                    lookInfo?.let {  looks[it.lookId] = it }
                }

                updateLooksOptionsMenu()
            }
    }

    private fun updateLooksOptionsMenu(

    ) {
        val options = requireView().findViewById<GridLayout>(R.id.vertical_options)
        options.removeAllViews()

        options.post {
            val imageWidth =
                (options.width - options.paddingStart - options.paddingEnd) / NUM_OF_ELEMENTS_IN_ROW

            looks.values.forEach { lookInfo ->
                val button = if (lookInfo.imagePreviewRef.isNotEmpty()) {
                    ImageButton(context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        background = null
                        id = lookInfo.lookId.hashCode()
                        setOnClickListener {
                            selectLook( requireView(), it, lookInfo)
                        }
                    }
                } else {
                    Button(context).apply {
                        text = lookInfo.name
                        background = null
                        id = lookInfo.lookId.hashCode()
                        setOnClickListener {
                            selectLook( requireView(), it, lookInfo)
                        }
                    }
                }

                val params = GridLayout.LayoutParams().apply {
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    height = ScreenUtil.dpToPx(100, requireContext())
                    width = imageWidth
                }

                params.setGravity(Gravity.START)

                options.addView(button, params)

                if (selectedLookViewId == button.id) {
                    selectSquareButton(button)
                }

                if (lookInfo.imagePreviewRef.isNotEmpty()) {

                    GlideApp.with(this)
                        .load(storage.getReference(lookInfo.imagePreviewRef))
                        .thumbnail()
                        .into(button as ImageButton)
                }
            }
        }
    }

    private fun selectLook(
        view: View,
        buttonView: View,
        lookInfo: LookInfo,
    ) {
        view.findViewById<View>(selectedLookViewId)
            ?.let { deselectButton(view.findViewById(selectedLookViewId)) }

        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        if (selectedLookViewId == buttonView.id) {
            listener.removeLook(lookInfo.lookId)
        } else {
            listener.applyLook(lookInfo)
            selectSquareButton(buttonView)
        }

        selectedLookViewId = buttonView.id
    }

    fun resetMenu() {
        selectedLookViewId = 0
        fetchLooks()
    }
}
