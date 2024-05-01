package cz.cvut.arfittingroom.activity

import android.R.attr.password
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import cz.cvut.arfittingroom.databinding.ActivityLoginBinding


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
            if(validateInput(usernameInput.text.toString().trim(), pwdInput.text.toString().trim())) {
                login(usernameInput.text.toString().trim(), pwdInput.text.toString().trim())
            }
        }

        binding.buttonSingUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.buttonReturn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }


    private fun validateInput(username: String, pwd: String): Boolean {
        var isValid = true

        usernameInput.error = null
        pwdInput.error = null

        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            isValid = false
        }


        if (pwd.isEmpty()) {
            pwdInput.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun login(username: String, pwd: String) {
        auth.signInWithEmailAndPassword("$username@glamartist.com", pwd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.println(Log.INFO, null, "User $username logged in")
                    Toast.makeText( baseContext, "Logged in Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ShowRoomActivity::class.java))
                } else {

                    Log.println(Log.ERROR, null, "createUserWithEmail:failure ${task.exception}")
                    val errorMsg = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            "Invalid password or username"
                        }

                        is FirebaseNetworkException -> {
                            "Network error"
                        }
                        else -> {
                            "Something went wrong, please, try again"
                        }
                    }
                    Toast.makeText(
                        baseContext,
                        errorMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}