package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import cz.cvut.arfittingroom.databinding.ActivityMainBinding
import cz.cvut.arfittingroom.utils.FileUtil.deleteTempFiles
import cz.cvut.arfittingroom.utils.ScreenUtil
import mu.KotlinLogging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth

        if (auth.currentUser != null) {
            startActivity(Intent(this, ShowRoomActivity::class.java))
        }

        super.onCreate(savedInstanceState)


        supportActionBar?.hide()
        ScreenUtil.screenHeight = Resources.getSystem().displayMetrics.heightPixels
        ScreenUtil.screenWidth = Resources.getSystem().displayMetrics.widthPixels

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        deleteTempFiles(applicationContext)


        binding.buttonLogIn.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }
        binding.buttonSingUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

}
