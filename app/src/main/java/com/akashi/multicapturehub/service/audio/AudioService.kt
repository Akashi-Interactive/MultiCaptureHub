package com.akashi.multicapturehub.service.audio

import com.akashi.multicapturehub.elements.Song
import android.content.Context
import android.media.MediaRecorder
import androidx.core.net.toUri
import java.io.File
import java.io.IOException

class AudioService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var audioFile: File? = null

    // Start recording audio
    fun startRecording(){
        if(!isRecording){ // If not recording
            mediaRecorder = MediaRecorder().apply { // Apply media recorder
                setAudioSource(MediaRecorder.AudioSource.MIC) // Set audio source
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Set output format
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Set audio encoder
                audioFile = createAudioFile() // Create audio file
                setOutputFile(audioFile?.absolutePath) // Set output file
                try { // Try to prepare
                    prepare() // Prepare
                } catch (e: IOException) { // Catch exception
                    e.printStackTrace() // Print stack trace
                }
                start() // Start recording
            }
            isRecording = true // Set recording to true
        }
    }

    // Stop recording audio
    fun stopRecording(): Song? {
        if (isRecording) { // If recording
            mediaRecorder?.apply { // Apply media recorder
                stop() // Stop recording
                release() // Release
            }
            isRecording = false // Set recording to false
            mediaRecorder = null // Set media recorder to null
            if(audioFile != null){ // If audio file exists
                return Song(audioFile?.name ?: "", audioFile?.absolutePath ?: "", audioFile?.path?.toUri() ?: "".toUri()) // Return song
            }
        }
        return null // Return null
    }

    // Create audio file
    private fun createAudioFile(): File {
        val directory = File(context.getExternalFilesDir(null), "AudioHub") // Get external files directory
        if (!directory.exists()) { // If directory does not exist
            directory.mkdirs() // Make directory
        }
        return File.createTempFile("AUDIO_", ".3gp", directory) // Create temp file
    }

    // Get if recording
    fun getIsRecording(): Boolean {
        return isRecording
    }

    // On stop
    fun onStop() {
        mediaRecorder?.release() // Release media recorder
        mediaRecorder = null // Set media recorder to null
    }

}