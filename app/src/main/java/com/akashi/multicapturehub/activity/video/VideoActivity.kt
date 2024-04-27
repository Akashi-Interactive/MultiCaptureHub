package com.akashi.multicapturehub.activity.video

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akashi.multicapturehub.R
import com.akashi.multicapturehub.service.video.VideoService

class VideoActivity : AppCompatActivity(){
    private lateinit var videoService: VideoService
    private lateinit var video_capture_button: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        videoService = VideoService(this)
        video_capture_button = findViewById(R.id.video_capture_button)

        video_capture_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                toggleRecording()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleRecording() {
        if (videoService.getIsRecording()) {
            videoService.stopRecording()
            Toast.makeText(this, "Video saved: ${videoService.getVideoFilePath()}", Toast.LENGTH_SHORT).show()
            video_capture_button.text = getString(R.string.start_capture)
        } else {
            videoService.startRecording()
            video_capture_button.text = getString(R.string.stop_capture)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}