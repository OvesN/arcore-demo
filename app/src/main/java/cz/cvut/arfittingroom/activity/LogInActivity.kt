package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.databinding.ActivityLoginBinding
import cz.cvut.arfittingroom.databinding.ActivityMainBinding
import cz.cvut.arfittingroom.databinding.ActivitySignupBinding

class LogInActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.buttonSingUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.buttonReturn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}