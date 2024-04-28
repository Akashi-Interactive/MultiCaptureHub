package com.akashi.multicapturehub.service.audio

import com.akashi.multicapturehub.Song
import android.content.Context
import android.media.MediaRecorder
import java.io.File
import java.io.IOException

class AudioService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var audioFile: File? = null

    fun startRecording(){
        if(!isRecording){
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                audioFile = createAudioFile()
                setOutputFile(audioFile?.absolutePath)
                try {
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                start()
            }
            isRecording = true
        }
    }

    fun stopRecording(): Song? {
        if (isRecording) {
            mediaRecorder?.apply {
                stop()
                release()
            }
            isRecording = false
            return Song(audioFile?.name ?: "", audioFile?.absolutePath ?: "")
        }
        return null
    }

    private fun createAudioFile(): File {
        val directory = File(context.getExternalFilesDir(null), "AudioHub")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File.createTempFile("AUDIO_", ".3gp", directory)
    }

    fun getIsRecording(): Boolean {
        return isRecording
    }
}