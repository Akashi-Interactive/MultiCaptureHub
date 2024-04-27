package com.akashi.multicapturehub.service.video

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VideoService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecordding = false
    private var videoFilePath: String? = null

    fun startRecording(){
        if(!isRecordding){
            mediaRecorder = MediaRecorder(context).apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoSize(1280, 720)
                setVideoFrameRate(30)

                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

                try{
                    val videoFile = File.createTempFile(
                        "VIDEO_${timeStamp}_",
                        ".mp4",
                        storageDir
                    )
                    videoFilePath = videoFile.absolutePath
                    setOutputFile(videoFile)
                    prepare()
                    start()
                    isRecordding = true
                } catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopRecording(){
        if(isRecordding){
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecordding = false
        }
    }

    fun getVideoFilePath(): String? {
        return videoFilePath
    }

    fun getIsRecording(): Boolean {
        return isRecordding
    }
}