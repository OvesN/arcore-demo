package cz.cvut.arfittingroom.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.databinding.ActivityMakeupEditorBinding
import cz.cvut.arfittingroom.service.MakeupEditorService
import cz.cvut.arfittingroom.service.MakeupService
import javax.inject.Inject

class MakeupEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMakeupEditorBinding
    @Inject
    lateinit var makeUpService: MakeupService
    @Inject
    lateinit var makeupEditorService: MakeupEditorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        // Inflate the layout for this activity
        binding = ActivityMakeupEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener{
            val intent = Intent(this, MakeupActivity::class.java)
            startActivity(intent)
        }
    }
}