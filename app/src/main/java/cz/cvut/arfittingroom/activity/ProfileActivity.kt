package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.databinding.ActivityLoginBinding
import cz.cvut.arfittingroom.databinding.ActivityProfileBinding

class ProfileActivity: AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.buttonLogOut.setOnClickListener {
            //TODO(Log out logic)

            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}