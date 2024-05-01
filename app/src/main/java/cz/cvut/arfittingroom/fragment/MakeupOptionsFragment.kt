package cz.cvut.arfittingroom.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.firestore.FirebaseFirestore
import cz.cvut.arfittingroom.R
import cz.cvut.arfittingroom.databinding.FragmentMultipleOptionsBinding
import cz.cvut.arfittingroom.model.MAKEUP_TYPES_COLLECTION

class MakeupOptionsFragment : Fragment() {
    private val binding: FragmentMultipleOptionsBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var firestore: FirebaseFirestore
    private val makeupOptions = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_multiple_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchMakeupOptions()
    }

    private fun fetchMakeupOptions() {
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
                    updateUI()
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI() {
        binding.options.removeAllViews()
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
            binding.options.addView(button)

            if (option != makeupOptions.last()) {
                val divider = View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT)
                    background = AppCompatResources.getDrawable(context, R.color.colorLightGrey)
                }
                binding.options.addView(divider)
            }
        }
    }
}
