package cz.cvut.arfittingroom.fragment

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.activity.ResourceListener
import cz.cvut.arfittingroom.model.MAKEUP_COLLECTION
import cz.cvut.arfittingroom.model.MAKEUP_TYPES_COLLECTION
import cz.cvut.arfittingroom.model.REF_ATTRIBUTE
import cz.cvut.arfittingroom.model.TYPE_ATTRIBUTE
import cz.cvut.arfittingroom.module.GlideApp

class MakeupOptionsFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val makeupOptions = mutableSetOf<String>()
    private var selectedOption: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_menu_multiple_options, container, false)
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
        options.removeAllViews()

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
                    fetchTypeOptions(option.lowercase())
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

    private fun fetchTypeOptions(type: String) {
        firestore.collection(MAKEUP_COLLECTION)
            .whereEqualTo(TYPE_ATTRIBUTE, type)
            .get()
            .addOnSuccessListener { result ->
                val imageRefs = result.map { document ->
                    document.getString(REF_ATTRIBUTE)!!
                }

                view?.let {
                    val options = it.findViewById<LinearLayout>(R.id.options)
                    options.removeAllViews()

                    val density = context?.resources?.displayMetrics?.density ?: 0f

                    imageRefs.forEach { ref ->
                        val imageView = ImageView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                (100 * density).toInt(),
                                (100 * density).toInt()
                            )
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            background = null
                            id = View.generateViewId()
                            setOnClickListener {
                                selectOption(this, type = type, ref = ref)

                            }
                        }

                        options.addView(imageView)

                        GlideApp.with(this)
                            .load(storage.getReference(ref))
                            .thumbnail()
                            .into(imageView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun selectOption(imageView: ImageView, ref: String, type: String) {
        val listener = context as? ResourceListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ImageListener")
            return
        }

        if (selectedOption == imageView) {
            imageView.background = null
            selectedOption = null

            listener.removeImage(type)
            return
        }

        selectedOption?.background = null

        val drawable = ContextCompat.getDrawable(imageView.context, R.drawable.border)
        imageView.background = drawable

        selectedOption = imageView

        listener.applyImage(type, ref)
    }

}

