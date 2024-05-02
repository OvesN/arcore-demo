package cz.cvut.arfittingroom.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.activity.ResourceListener
import cz.cvut.arfittingroom.model.LOOKS_COLLECTION

class LooksOptionsFragment : Fragment() {

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
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_horizontal_scroll, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { fetchLooks(it) }
    }

    private fun fetchLooks(view: View) {
        firestore.collection(LOOKS_COLLECTION).get().addOnSuccessListener { result ->

            val looksId = result.map{it.id}
            updateLooksOptionsMenu(view, looksId)
        }
    }

    private fun updateLooksOptionsMenu(view: View, lookIds: List<String>) {
        val options = view.findViewById<LinearLayout>(R.id.options)
        options.removeAllViews()

        for (lookId in lookIds) {
            val button = Button(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = lookId
                isAllCaps = false
                background = null
                id = View.generateViewId()
                setOnClickListener {
                    applyLook(lookId)
                }
            }
            options.addView(button)
            options.addView(createDivider())

        }
    }

    private fun applyLook(lookId: String){
        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }

        listener.applyLook(lookId)
    }

    private fun createDivider() =
        View(context).apply {
            layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
            background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
        }
}