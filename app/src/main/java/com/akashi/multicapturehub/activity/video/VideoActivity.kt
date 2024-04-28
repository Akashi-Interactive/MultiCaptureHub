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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityVideoBinding.inflate(layoutInflater)
        videoService = VideoService(this, viewBinding, this)

        setContentView(viewBinding.root)

        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            videoService.stopRecording()
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }

        if(allPermissionsGranted()){
            videoService.startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        videoCaptureButton = findViewById(R.id.audio_capture_button)

        videoCaptureButton.setOnClickListener {
                toggleRecording()
        }
    }

    private fun toggleRecording() {
        if (videoService.getIsRecording()) {
            videoService.stopRecording()
            Toast.makeText(this, "Video saved: ${videoService.getVideoFilePath()}", Toast.LENGTH_SHORT).show()
            videoCaptureButton.text = getString(R.string.start_capture)
        } else {
            videoService.startRecording()
            videoCaptureButton.text = getString(R.string.stop_capture)
        }
    }
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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        videoService.onDestroy()
        super.onDestroy()
    }

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