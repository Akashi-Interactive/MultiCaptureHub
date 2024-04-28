package com.akashi.multicapturehub.activity.gallery

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.akashi.multicapturehub.R
import com.akashi.multicapturehub.activity.video.VideoActivity
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.akashi.multicapturehub.activity.audio.AudioActivity
import com.akashi.multicapturehub.elements.Video

class GalleryActivity : AppCompatActivity() {

    private lateinit var galleryListLayout: ConstraintLayout
    private lateinit var scrollView: ScrollView

    private val videosUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    private val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.RELATIVE_PATH
    )

    private lateinit var videoButton: ImageButton
    private lateinit var audioButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        scrollView = findViewById(R.id.scrollGallery)
        galleryListLayout = findViewById(R.id.gallery)

        videoButton = findViewById(R.id.video_record)
        audioButton = findViewById(R.id.audio_record)

        videoButton.setOnClickListener {
            val intent = Intent(this, VideoActivity::class.java)
            startActivity(intent)
        }

        audioButton.setOnClickListener {
            val intent = Intent(this, AudioActivity::class.java)
            startActivity(intent)
        }

        loadMediaFiles()
    }

    private fun loadMediaFiles() {
        // Cargar tanto archivos de audio como de video
        val audioFiles = loadAudioFiles()
        val videoFiles = loadVideoFiles()

        // Crear vistas para archivos de audio
        for (audioUri in audioFiles) {
            val audioItemView = layoutInflater.inflate(R.layout.item_audio, galleryListLayout, false)
            val audioTitleTextView = audioItemView.findViewById<TextView>(R.id.song_title)
            audioTitleTextView.text = audioUri.toString() // Mostrar el título del archivo de audio
            galleryListLayout.addView(audioItemView)
        }

        // Crear vistas para archivos de video
        for (videoUri in videoFiles) {
            val videoItemView = layoutInflater.inflate(R.layout.item_video, galleryListLayout, false)
            val videoTitleTextView = videoItemView.findViewById<TextView>(R.id.video_title)
            videoTitleTextView.text = videoUri.toString() // Mostrar el título del archivo de video
            galleryListLayout.addView(videoItemView)
        }
    }

    private fun loadAudioFiles(): List<Uri> {
        val audioFiles = mutableListOf<Uri>()
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.RELATIVE_PATH
        )

        val cursor = contentResolver.query(audioUri, projection, null, null, null)
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(pathColumn)

                val contentUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                audioFiles.add(contentUri)
            }
        }

        return audioFiles
    }

    private fun loadVideoFiles(): List<Uri> {
        val videoFiles = mutableListOf<Uri>()

        val cursor = contentResolver.query(videosUri, projection, null, null, null)
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(pathColumn)

                val contentUri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                videoFiles.add(contentUri)
            }
        }

        return videoFiles
    }
}