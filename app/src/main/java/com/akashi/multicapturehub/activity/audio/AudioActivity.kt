package com.akashi.multicapturehub.activity.audio

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.akashi.multicapturehub.R
import com.akashi.multicapturehub.service.audio.AudioService

class AudioActivity :AppCompatActivity() {
    private lateinit var audioService: AudioService
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        audioService = AudioService(this)

        startButton = findViewById(R.id.audio_capture_button)

        startButton.setOnClickListener {
            toggleAudioRecord()
        }
    }

    private fun toggleAudioRecord(){
        if(audioService.getIsRecording()){
            audioService.stopRecording()
            startButton.text = getString(R.string.start_capture)
        } else {
            audioService.startRecording()
            startButton.text = getString(R.string.stop_capture)
        }
    }
}