package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.databinding.ActivityLoginBinding
import cz.cvut.arfittingroom.databinding.ActivityMainBinding
import cz.cvut.arfittingroom.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySignupBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.buttonSignUp.setOnClickListener {
        }

        binding.buttonReturn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}