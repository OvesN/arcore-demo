package com.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.cvut.arfittingroom.R
import com.cvut.arfittingroom.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import io.github.muddz.styleabletoast.StyleableToast

/**
 * Log in activity
 *
 * @author Veronika Ovsyannikova
 */
class LogInActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var auth: FirebaseAuth
    private lateinit var fileStore: FirebaseFirestore
    private lateinit var usernameInput: EditText
    private lateinit var pwdInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileStore = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        setContentView(binding.root)

        usernameInput = binding.username
        pwdInput = binding.pwd

        binding.buttonLogIn.setOnClickListener {
            if (validateInput(usernameInput.text.toString().trim(), pwdInput.text.toString().trim())) {
                login(usernameInput.text.toString().trim(), pwdInput.text.toString().trim())
            }
        }

        binding.buttonSingUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.buttonReturn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun validateInput(
        username: String,
        pwd: String,
    ): Boolean {
        usernameInput.error = null
        pwdInput.error = null

        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            return false
        }

        if (pwd.isEmpty()) {
            pwdInput.error = "Password is required"
            return false
        }

        return true
    }

    private fun login(
        username: String,
        pwd: String,
    ) {
        auth.signInWithEmailAndPassword("$username@glamartist.com", pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.println(Log.INFO, null, "User $username logged in")
                    StyleableToast.makeText(baseContext, "Logged in Successfully!", R.style.mytoast).show()
                    startActivity(Intent(this, FittingRoomActivity::class.java))
                    finish()
                } else {
                    Log.println(Log.ERROR, null, "createUserWithEmail:failure ${task.exception}")
                    val errorMsg =
                        when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> "Invalid password or username"

                            is FirebaseNetworkException -> "Network error"
                            else -> "Something went wrong, please, try again"
                        }

                    StyleableToast.makeText(baseContext, errorMsg, R.style.mytoast).show()
                }
            }
    }
}
