package cz.cvut.arfittingroom.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.model.MAKEUP_TYPES_COLLECTION

class MakeupSingleOptionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_single_option, container, false)
    }

    private lateinit var firestore: FirebaseFirestore
    private val makeupOptions = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { fetchMakeupOptions(it) }
    }

    private fun fetchMakeupOptions(view: View) {
        firestore.collection(MAKEUP_TYPES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                makeupOptions.clear()
                for (document in result) {
                    val docId = document.id
                    val capitalizedDocId = docId.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }
                    makeupOptions.add(capitalizedDocId)
                }

                if (isAdded) {
                    updateUI(view)
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(view: View) {

        val options = view.findViewById<LinearLayout>(R.id.options)

        for (option in makeupOptions) {
            val button = Button(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = option
                isAllCaps = false
                background = null
                id = View.generateViewId()
                setOnClickListener {
                    Toast.makeText(context, "$option clicked", Toast.LENGTH_SHORT).show()
                }
            }
            options.addView(button)

            if (option != makeupOptions.last()) {
                val divider = View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT)
                    background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
                }

                options.addView(divider)
            }
        }
    }
}