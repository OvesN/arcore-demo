package com.cvut.arfittingroom.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.ResourceListener
import com.cvut.arfittingroom.model.AUTHOR_ATTRIBUTE
import com.cvut.arfittingroom.model.CREATED_AT_ATTRIBUTE
import com.cvut.arfittingroom.model.IS_PUBLIC_ATTRIBUTE
import com.cvut.arfittingroom.model.LOOKS_COLLECTION
import com.cvut.arfittingroom.model.NUM_OF_ELEMENTS_IN_ROW_BIG_MENU
import com.cvut.arfittingroom.model.to.LookTO
import com.cvut.arfittingroom.module.GlideApp
import com.cvut.arfittingroom.utils.UIUtil.deselectLookButton
import com.cvut.arfittingroom.utils.UIUtil.selectLookButton
import com.cvut.arfittingroom.utils.UIUtil.showLookInfoPopup
import com.cvut.arfittingroom.utils.currentUserUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import io.github.muddz.styleabletoast.StyleableToast

class LooksMenuFragment : Fragment() {
    private var selectedLookTO = LookTO()
    private val looks = mutableMapOf<String, LookTO>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var filter: Filter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        filter =
            Filter.or(
                Filter.equalTo(IS_PUBLIC_ATTRIBUTE, true),
                Filter.equalTo(
                    AUTHOR_ATTRIBUTE,
                    auth.currentUserUsername(),
                ),
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_look_menu, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.vertical_scroll_view).visibility = View.VISIBLE
        view.findViewById<GridLayout>(R.id.vertical_options).columnCount =
            NUM_OF_ELEMENTS_IN_ROW_BIG_MENU
        view.findViewById<CheckBox>(R.id.my_looks_filter)
            .setOnCheckedChangeListener { _, isChecked ->
                filter =
                    if (isChecked) {
                        Filter.equalTo(
                            AUTHOR_ATTRIBUTE,
                            auth.currentUserUsername(),
                        )
                    } else {
                        Filter.or(
                            Filter.equalTo(IS_PUBLIC_ATTRIBUTE, true),
                            Filter.equalTo(
                                AUTHOR_ATTRIBUTE,
                                auth.currentUserUsername(),
                            ),
                        )
                    }
            }
    }

    fun fetchLooks() {
        looks.clear()
        firestore.collection(LOOKS_COLLECTION)
            .where(
                filter,
            )
            .orderBy(CREATED_AT_ATTRIBUTE,  Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                result.documents.forEach { look ->
                    val lookTO = look.toObject(LookTO::class.java)
                    lookTO?.let { looks[it.lookId] = it }
                }

                updateLooksOptionsMenu()
            }
            .addOnFailureListener { ex ->
                StyleableToast.makeText(
                    requireContext(),
                    ex.message,
                    Toast.LENGTH_SHORT,
                    R.style.mytoast,
                ).show()
            }
    }

    private fun updateLooksOptionsMenu() {
        val options = requireView().findViewById<GridLayout>(R.id.vertical_options)
        options.removeAllViews()

        options.post {
            val imageWidth =
                (options.width - options.paddingStart - options.paddingEnd) / NUM_OF_ELEMENTS_IN_ROW_BIG_MENU

            looks.values.forEach { lookInfo ->
                val button =
                    if (lookInfo.previewRef.isNotEmpty()) {
                        ImageButton(context).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            background = ContextCompat.getDrawable(context, R.drawable.head_model)
                            id = lookInfo.lookId.hashCode()
                            setOnClickListener {
                                selectLook(requireView(), it, lookInfo)
                            }
                        }
                    } else {
                        Button(context).apply {
                            text = lookInfo.name
                            background = null
                            id = lookInfo.lookId.hashCode()
                            setOnClickListener {
                                selectLook(requireView(), it, lookInfo)
                            }
                            setOnLongClickListener {view ->
                                showLookInfoMenu(view)
                                true
                            }
                        }
                    }

                val params =
                    GridLayout.LayoutParams().apply {
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        height = imageWidth
                        width = imageWidth
                    }

                params.setGravity(Gravity.START)

                options.addView(button, params)

                if (selectedLookTO.lookId.hashCode() == button.id) {
                    selectLookButton(button)
                }

                if (lookInfo.previewRef.isNotEmpty()) {
                    GlideApp.with(this)
                        .load(storage.getReference(lookInfo.previewRef))
                        .thumbnail()
                        .into(button as ImageButton)
                }
            }
        }
    }

    private fun selectLook(
        view: View,
        buttonView: View,
        lookTO: LookTO,
    ) {
        view.findViewById<View>(selectedLookTO.lookId.hashCode())
            ?.let { deselectLookButton(it) }

        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        selectedLookTO = if (selectedLookTO.lookId.hashCode() == buttonView.id) {
            listener.removeLook(lookTO.lookId)
            LookTO()
        } else {
            listener.applyLook(lookTO)
            selectLookButton(buttonView)
            lookTO
        }
    }

    fun resetMenu() {
        selectedLookTO = LookTO()
        fetchLooks()
    }

    private fun showLookInfoMenu(view: View) {
        showLookInfoPopup(
            requireContext(),
            selectedLookTO,
            isAuthor = selectedLookTO.author == auth.currentUserUsername(),
            view,
            onLookDelete = {
                deleteLook(selectedLookTO.lookId)
                val listener = context as? ResourceListener
                listener?.removeLook(selectedLookTO.lookId)
                resetMenu()
            },
            onChangeIsPublic = { isPublic ->
                if (selectedLookTO.lookId.isNotEmpty()) {
                    changeLookPublicity(
                        lookId = selectedLookTO.lookId,
                        isPublic
                    )
                }

            }
        )
    }

    private fun deleteLook(lookId: String) {
        firestore.collection(LOOKS_COLLECTION).document(lookId)
            .get()
            .addOnSuccessListener { result ->
                val lookTo = result.toObject<LookTO>()
                lookTo?.let {
                    storage.getReference(it.previewRef).delete()
                    val folderRef = storage.getReference("$LOOKS_COLLECTION/${lookTo.lookId}")
                    folderRef.listAll()
                        .addOnSuccessListener { listResult ->
                            val items = listResult.items
                            items.map { item ->
                                item.delete()
                            }
                        }
                        .addOnFailureListener { ex ->
                            StyleableToast.makeText(
                                requireContext(),
                                ex.message,
                                Toast.LENGTH_SHORT,
                                R.style.mytoast,
                            ).show()
                        }
                }
            }
            .addOnFailureListener { ex ->
                StyleableToast.makeText(
                    requireContext(),
                    ex.message,
                    Toast.LENGTH_SHORT,
                    R.style.mytoast,
                ).show()
            }

        firestore.collection(LOOKS_COLLECTION).document(lookId).delete()
    }

    private fun changeLookPublicity(lookId: String, isPublic: Boolean) {

        val lookDoc = firestore.collection(LOOKS_COLLECTION).document(lookId)

        lookDoc.get().addOnSuccessListener {
            val lookTo = it.toObject<LookTO>()
            lookTo?.let { look ->
                look.isPublic = isPublic
                lookDoc.set(look)
            }
        }
    }

    fun getLook(lookId: String, onSuccess: (LookTO) -> Unit = {}) {
        firestore.collection(LOOKS_COLLECTION).document(lookId)
            .get().addOnSuccessListener {
            val lookTo = it.toObject<LookTO>()
            lookTo?.let { onSuccess(lookTo) }
        }.addOnFailureListener { ex ->
            StyleableToast.makeText(
                requireContext(),
                ex.message,
                Toast.LENGTH_SHORT,
                R.style.mytoast,
            ).show()
        }
    }

    fun selectLook(lookId: String) {
        getLook(lookId) { lookTO ->
            requireView().findViewById<View>(selectedLookTO.lookId.hashCode())
                ?.let { deselectLookButton(it) }
            selectedLookTO = lookTO
            requireView().findViewById<View>(selectedLookTO.lookId.hashCode())?.let {
                selectLookButton(it)
            }
        }
    }

    fun getSelectedLookId() = selectedLookTO.lookId

    fun getSelectedLookTO() = selectedLookTO
}
