package cz.cvut.arfittingroom.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.cvut.arfittingroom.ARFittingRoomApplication
import cz.cvut.arfittingroom.databinding.ActivityModelEditorBinding
import cz.cvut.arfittingroom.service.Editor3DService
import javax.inject.Inject

class ModelEditorActivity : AppCompatActivity() {
    @Inject
    lateinit var editorService: Editor3DService
    private lateinit var binding: ActivityModelEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ARFittingRoomApplication).appComponent.inject(this)

        // Inflate the layout for this activity
        binding = ActivityModelEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use the FragmentManager to find the AR Fragment by ID
//        arFragment = supportFragmentManager.findFragmentById(R.id.face_fragment) as FaceArFragment
    }
}