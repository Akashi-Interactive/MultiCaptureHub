package com.akashi.multicapturehub.activity.audio

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akashi.multicapturehub.R
import com.akashi.multicapturehub.activity.gallery.GalleryActivity
import com.akashi.multicapturehub.service.audio.AudioService

class AudioActivity :AppCompatActivity() {
    private lateinit var audioService: AudioService
    private lateinit var toggleButton: Button
    private lateinit var backButton: Button

    // During activity creation
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_audio) // Set audio activity to display

        audioService = AudioService(this) // Create new audio service

        toggleButton = findViewById(R.id.audio_capture_button) // Look for toggle button
        backButton = findViewById(R.id.back_audio_button) // Look for back button

        if(!allPermissionsGranted()){ // If not all permissions are granted
            ActivityCompat.requestPermissions( // Request permissions
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS // With the required permissions and request code
            )
        }

        toggleButton.setOnClickListener { // Set method to call on clicked for toggle
            toggleAudioRecord()
        }

        backButton.setOnClickListener {  // Set intent process on back button
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }
    }

    // Toggle method
    private fun toggleAudioRecord(){
        // If currently recording an audio
        if(audioService.getIsRecording()){
            audioService.stopRecording() // Stop the current record
            toggleButton.text = getString(R.string.start_capture) // Change text for toggle button
        } else { // If not recording audio
            audioService.startRecording() // Start new record
            toggleButton.text = getString(R.string.stop_capture) // Change text for toggle button
        }
    }

    // Check if all permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Constants
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    }
}