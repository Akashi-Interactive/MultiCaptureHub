package com.akashi.multicapturehub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.akashi.multicapturehub.activity.video.VideoActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val intent = Intent(this, VideoActivity::class.java)
        startActivity(intent)
    }
}