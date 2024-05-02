package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
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
    private val binding: ActivityMainBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        if (auth.currentUser != null) {
            startActivity(Intent(this, ShowRoomActivity::class.java))
            finish()
            return
        }

        supportActionBar?.hide()
        ScreenUtil.screenHeight = Resources.getSystem().displayMetrics.heightPixels
        ScreenUtil.screenWidth = Resources.getSystem().displayMetrics.widthPixels

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
