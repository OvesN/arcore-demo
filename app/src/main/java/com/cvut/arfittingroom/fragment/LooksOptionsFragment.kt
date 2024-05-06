package com.cvut.arfittingroom.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.ResourceListener
import com.cvut.arfittingroom.model.LOOKS_COLLECTION
import com.cvut.arfittingroom.utils.UIUtil.createDivider
import com.cvut.arfittingroom.utils.UIUtil.deselectButton
import com.cvut.arfittingroom.utils.UIUtil.selectSquareButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class LooksOptionsFragment : Fragment() {
    private var selectedLookViewId: Int = 0
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
    ): View? = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { fetchLooks(it) }
    }

    private fun fetchLooks(view: View) {
        firestore.collection(LOOKS_COLLECTION).get().addOnSuccessListener { result ->

            val looksId = result.map { it.id }
            updateLooksOptionsMenu(view, looksId)
        }
    }

    private fun updateLooksOptionsMenu(
        view: View,
        lookIds: List<String>,
    ) {
        val options = view.findViewById<LinearLayout>(R.id.horizontal_options)
        options.removeAllViews()

        for (lookId in lookIds) {
            val button =
                Button(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    text = lookId
                    isAllCaps = false
                    background = null
                    id = lookId.hashCode()
                    setOnClickListener {
                        selectLook(view, it, lookId)
                    }
                }
            options.addView(button)
            options.addView(createDivider(requireContext()))
        }
    }

    private fun selectLook(
        view: View,
        buttonView: View,
        lookId: String,
    ) {
        deselectButton(view.findViewById(selectedLookViewId))
        selectSquareButton(buttonView)

        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        if (selectedLookViewId == buttonView.id) {
            listener.removeLook(lookId)
        } else {
            listener.applyLook(lookId)
        }

        selectedLookViewId = view.id
    }
}
