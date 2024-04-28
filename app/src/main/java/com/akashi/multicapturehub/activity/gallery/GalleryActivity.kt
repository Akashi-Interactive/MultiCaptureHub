package com.akashi.multicapturehub.activity.gallery

import android.content.Intent
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
import android.widget.TextView
import com.akashi.multicapturehub.elements.Video

class GalleryActivity : AppCompatActivity() {

    private lateinit var galleryListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        galleryListLayout = findViewById(R.id.gallery_list)

        loadVideos()

        val videoRecordButton: ImageButton = findViewById(R.id.video_record)
        videoRecordButton.setOnClickListener {
            val intent = Intent(this, VideoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadVideos() {
        val videosUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.RELATIVE_PATH
        )
        val selection = "${MediaStore.Video.Media.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf("Movies/VideoHub")
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(videosUri, projection, selection, selectionArgs, sortOrder)
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    val videoName =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                    val videoLocation =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH))
                    val videoUri = Uri.withAppendedPath(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    )
                    val video = Video(videoName, videoLocation, videoUri)

                    val videoItemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_video, galleryListLayout, false)
                    val videoTitleTextView: TextView = videoItemView.findViewById(R.id.video_title)
                    val playButton: Button = videoItemView.findViewById(R.id.button_play)

                    videoTitleTextView.text = video.name

                    playButton.setOnClickListener {
                        val playIntent = Intent(Intent.ACTION_VIEW, video.videoUri)
                        playIntent.setDataAndType(video.videoUri, "video/*")
                        startActivity(playIntent)
                    }

                    galleryListLayout.addView(videoItemView)
                }
            }
    }
}