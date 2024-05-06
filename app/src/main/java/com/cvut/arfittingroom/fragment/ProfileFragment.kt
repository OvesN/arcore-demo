package com.cvut.arfittingroom.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.activity.MainActivity
import com.cvut.arfittingroom.activity.UIChangeListener
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        view.findViewById<TextView>(R.id.username).text = auth.currentUser?.email?.substringBefore('@')

        view.findViewById<Button>(R.id.back_to_editor_button).setOnClickListener {
            showMainLayout()
        }
        view.findViewById<Button>(R.id.log_out_button).setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            activity?.finish()
        }
    }

    private fun showMainLayout() {
        val listener = context as? UIChangeListener
        if (listener == null) {
            Log.println(Log.ERROR, null, "Activity does not implement ResourceListener")
            return
        }
        listener.showMainLayout()
    }
}
