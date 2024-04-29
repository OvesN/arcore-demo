package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import cz.cvut.arfittingroom.databinding.ActivityLoginBinding
import cz.cvut.arfittingroom.databinding.ActivityMainBinding
import cz.cvut.arfittingroom.databinding.ActivitySignupBinding

class LogInActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var fileStore: FirebaseFirestore
    private lateinit var usernameInput: EditText
    private lateinit var pwdInput: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileStore = FirebaseFirestore.getInstance()

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        usernameInput = binding.username
        pwdInput = binding.pwd


        binding.buttonSingUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.buttonReturn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}