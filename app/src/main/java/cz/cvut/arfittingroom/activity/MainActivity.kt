package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.databinding.ActivityMainBinding
import mu.KotlinLogging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val logger = KotlinLogging.logger{}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.buttonMakeup.setOnClickListener {
            logger.info { "Makeup button clicked" }
            startActivity(Intent(this, ShowRoomActivity::class.java))
        }

    }
}
