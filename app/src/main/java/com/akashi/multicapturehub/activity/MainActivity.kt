package com.akashi.multicapturehub.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.akashi.multicapturehub.activity.gallery.GalleryActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, GalleryActivity::class.java)
        startActivity(intent)
    }
}