package com.cvut.arfittingroom.activity

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.cvut.arfittingroom.databinding.ActivityMainBinding
import com.cvut.arfittingroom.utils.FileUtil.deleteTempFiles
import com.cvut.arfittingroom.utils.ScreenUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        ScreenUtil.screenHeight = Resources.getSystem().displayMetrics.heightPixels
        ScreenUtil.screenWidth = Resources.getSystem().displayMetrics.widthPixels
        deleteTempFiles(applicationContext)

        if (auth.currentUser != null) {
            startActivity(Intent(this, ShowRoomActivity::class.java))
            finish()
            return
        }

        supportActionBar?.hide()

        setContentView(binding.root)

        binding.buttonLogIn.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }
        binding.buttonSingUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
}