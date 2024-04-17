package cz.cvut.arfittingroom.activity

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.databinding.ActivityMainBinding
import cz.cvut.arfittingroom.utils.FileUtil.deleteTempMaskTextureBitmap
import cz.cvut.arfittingroom.utils.ScreenUtil
import mu.KotlinLogging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val logger = KotlinLogging.logger{}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenUtil.screenHeight = Resources.getSystem().displayMetrics.heightPixels
        ScreenUtil.screenWidth = Resources.getSystem().displayMetrics.widthPixels

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        deleteTempMaskTextureBitmap(applicationContext)

        binding.buttonMakeup.setOnClickListener {
            logger.info { "Makeup button clicked" }
            startActivity(Intent(this, ShowRoomActivity::class.java))
        }

    }
}
