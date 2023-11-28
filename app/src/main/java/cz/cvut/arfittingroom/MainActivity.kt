package cz.cvut.arfittingroom

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.databinding.ActivityGlassesBinding
import cz.cvut.arfittingroom.databinding.ActivityMainBinding
import mu.KotlinLogging
import kotlin.jvm.internal.Intrinsics.Kotlin

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val logger = KotlinLogging.logger{}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.buttonMakeup.setOnClickListener {
            logger.info { "Makeup button clicked" }
            startActivity(Intent(this, MakeupActivity::class.java))
        }

        binding.buttonGlasses.setOnClickListener {
            logger.info { "Glasses button clicked" }
            startActivity(Intent(this, GlassesActivity::class.java))
        }

    }
}
