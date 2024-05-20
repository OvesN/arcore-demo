package com.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.cvut.arfittingroom.databinding.ActivityMainBinding
import com.cvut.arfittingroom.utils.FileUtil.deleteTempFiles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Main activity
 *
 * @author Veronika Ovsyannikova
 */
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        deleteTempFiles(applicationContext)

        if (auth.currentUser != null) {
            handleIntent(intent)
            startActivity(Intent(this, FittingRoomActivity::class.java))
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        intent.data?.let { uri ->
            if (uri.toString().startsWith("http://www.glamartist.com/look")) {
                val lookId = uri.getQueryParameter("lookId")
                if (lookId != null) {
                    navigateToFittingRoom(lookId)
                }
            }
        }
    }

    private fun navigateToFittingRoom(lookId: String) {
        val intent = Intent(this, FittingRoomActivity::class.java)
        intent.putExtra("LOOK_ID", lookId)
        startActivity(intent)
    }

}
