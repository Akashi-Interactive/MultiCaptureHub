package com.akashi.multicapturehub.activity.video

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akashi.multicapturehub.R
import com.akashi.multicapturehub.activity.gallery.GalleryActivity
import com.akashi.multicapturehub.service.video.VideoService
import com.akashi.multicapturehub.databinding.ActivityVideoBinding


class VideoActivity : AppCompatActivity(){
    private lateinit var viewBinding: ActivityVideoBinding
    private lateinit var videoService: VideoService
    private lateinit var videoCaptureButton: Button
    private lateinit var backButton: Button

    // On Create activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityVideoBinding.inflate(layoutInflater) // Inflate the layout
        videoService = VideoService(this, viewBinding, this) // Create a new VideoService

        setContentView(viewBinding.root) // Set the content view

        if(allPermissionsGranted()){ // If all permissions are granted
            videoService.startCamera() // Start the camera
        } else { // If not
            ActivityCompat.requestPermissions( // Request permissions
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS // With the required permissions and request code
            )
        }

        backButton = findViewById(R.id.back_button) // Get the back button
        videoCaptureButton = findViewById(R.id.audio_capture_button) // Get the video capture button

        backButton.setOnClickListener { // On back button click
            videoService.stopRecording() // Stop recording
            val intent = Intent(this, GalleryActivity::class.java) // Create a new intent
            startActivity(intent) // Start the gallery activity
        }

        videoCaptureButton.setOnClickListener { // On video capture button click
                toggleRecording() // Toggle recording
        }
    }

    // Toggle recording
    private fun toggleRecording() {
        if (videoService.getIsRecording()) { // If recording
            videoService.stopRecording() // Stop recording
            Toast.makeText(this, "Video saved: ${videoService.getVideoFilePath()}", Toast.LENGTH_SHORT).show() // Show a toast
            videoCaptureButton.text = getString(R.string.start_capture) // Set the button text to start capture
        } else { // If not recording
            videoService.startRecording() // Start recording
            videoCaptureButton.text = getString(R.string.stop_capture) // Set the button text to stop capture
        }
    }

    // On request permissions result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                videoService.startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Check if all permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // On destroy
    override fun onDestroy() {
        videoService.onDestroy()
        super.onDestroy()
    }

    // Constants
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}