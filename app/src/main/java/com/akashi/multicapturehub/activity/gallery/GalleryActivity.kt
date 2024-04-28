package com.akashi.multicapturehub.activity.gallery

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.akashi.multicapturehub.R
import com.akashi.multicapturehub.activity.video.VideoActivity

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val videoRecordButton: ImageButton = findViewById(R.id.video_record)
        videoRecordButton.setOnClickListener {
            val intent = Intent(this, VideoActivity::class.java)
            startActivity(intent)
        }
    }
}