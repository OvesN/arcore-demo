package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import cz.cvut.arfittingroom.databinding.ActivitySignupBinding
import cz.cvut.arfittingroom.model.USERS_COLLECTION

private val USERNAME_REGEX = "^[a-zA-Z0-9]+$".toRegex()


class SignUpActivity : AppCompatActivity() {
    private val binding: ActivitySignupBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var auth: FirebaseAuth
    private lateinit var fileStore: FirebaseFirestore
    private lateinit var usernameInput: EditText
    private lateinit var pwdInput: EditText
    private lateinit var repeatPwdInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        fileStore = FirebaseFirestore.getInstance()

        usernameInput = binding.username
        pwdInput = binding.pwd
        repeatPwdInput = binding.pwdRepeat

        setContentView(binding.root)

        binding.buttonSignUp.setOnClickListener {
            if (validateInput(
                    usernameInput.text.trim().toString(),
                    pwdInput.text.trim().toString(),
                    repeatPwdInput.text.trim().toString()
                )
            ) {
                createAccount(
                    "${usernameInput.text.trim()}@glamartist.com",
                    pwdInput.text.trim().toString(),
                    usernameInput.text.trim().toString()
                )
            }
        }

        binding.buttonReturn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun validateInput(username: String, pwd: String, repeatPwd: String): Boolean {
        var isValid = true

        usernameInput.error = null
        pwdInput.error = null
        repeatPwdInput.error = null

        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            isValid = false
        }

        if (!USERNAME_REGEX.matches(username)) {
            usernameInput.error = "Username should not have any special characters"
            isValid = false
        }

        if (pwd.isEmpty()) {
            pwdInput.error = "Password is required"
            isValid = false
        }

        if (repeatPwd.isEmpty()) {
            repeatPwdInput.error = "Password repeat is required"
            isValid = false
        } else if (pwd.length < 6) {
            pwdInput.error = "Password must be >= 6 characters"
            isValid = false
        }


        if (username.length > 20) {
            pwdInput.error = "Username Must be < 20 Characters"
            isValid = false
        }

        if (pwd != repeatPwd) {
            repeatPwdInput.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun createAccount(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    Log.println(Log.INFO, null, "User with $username succsesfully created")
                    val documentReference: DocumentReference =
                        fileStore.collection(USERS_COLLECTION).document(currentUser?.uid ?: "")
                    val user: MutableMap<String, Any> = HashMap()
                    user["username"] = username
                    documentReference.set(user).addOnSuccessListener {
                        Log.println(
                            Log.INFO,
                            null,
                            "onSuccess: user Profile is created for $username"
                        )
                        startActivity(Intent(this, ShowRoomActivity::class.java))
                        finish()
                    }.addOnFailureListener { e -> Log.println(Log.ERROR, null, "onFailure: $e") }

                } else {
                    Log.println(Log.ERROR, null, "createUserWithEmail:failure ${task.exception}")
                    val errorMsg = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> {
                            "This username is taken! Try another one"
                        }

                        is FirebaseNetworkException -> {
                            "Network error"
                        }
                        else -> {
                            "Something went wrong, please, try again"
                        }
                    }

                    val toast = Toast.makeText(
                        baseContext,
                        errorMsg,
                        Toast.LENGTH_SHORT,
                    )
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)

                    toast.show()
                }
            }
    }

}