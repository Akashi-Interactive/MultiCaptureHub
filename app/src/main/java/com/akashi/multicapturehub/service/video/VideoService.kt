package com.akashi.multicapturehub.service.video

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.camera.lifecycle.ProcessCameraProvider
import java.text.SimpleDateFormat
import android.content.ContentValues
import android.provider.MediaStore
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import com.akashi.multicapturehub.databinding.ActivityVideoBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LifecycleOwner
import java.nio.ByteBuffer
import java.util.Locale

typealias LumaListener = (luma: Double) -> Unit
class VideoService(private val context: Context,
                   private val viewBinding: ActivityVideoBinding,
                   private val lifecycleOwner: LifecycleOwner) {

    private var isRecording = false
    private var videoFilePath: String? = null
    private lateinit var cameraExecutor: ExecutorService
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    fun startCamera(){
        cameraExecutor = Executors.newSingleThreadExecutor() // Create a new single thread executor

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context) // Get the camera provider future

        cameraProviderFuture.addListener(Runnable{ // Add a listener
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get() // Get the camera provider

            val preview = Preview.Builder() // Create a new preview builder
                .build() // Build the preview
                .also { // Also
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider) // Set the surface provider
                }

            val recorder = Recorder.Builder() // Create a new recorder builder
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST)) // Set the quality selector
                .build() // Build the recorder
            videoCapture = VideoCapture.withOutput(recorder) // Create a new video capture with output

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // Get the default back camera

            try { // Try
                cameraProvider.unbindAll() // Unbind all

                cameraProvider.bindToLifecycle( // Bind to lifecycle
                    lifecycleOwner, cameraSelector, preview, videoCapture) // With the lifecycle owner, camera selector, preview, and video capture
            } catch(exc: Exception) { // Catch exception
                Log.e(TAG, "Use case binding failed", exc) // Log error
            }

        }, ContextCompat.getMainExecutor(context)) // With the main executor

    }

    // Start recording
    fun startRecording(){
        val videoCapture = this.videoCapture ?: return // Get the video capture or return
        Log.w("String","isRecording: $isRecording") // Log isRecording

        if(!isRecording){ // If not recording
            Log.w("String","startRecording3") // Log start recording

            val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US) // Get the date format
                .format(System.currentTimeMillis()) // Format the current time
            val contentValues = ContentValues().apply { // Apply content values
                put(MediaStore.MediaColumns.DISPLAY_NAME, name) // Put display name
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4") // Put mime type
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) { // If the build version is greater than P
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VideoHub") // Put relative path
                }
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions // Create a new media store output options
                .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI) // With the content resolver and external content uri
                .setContentValues(contentValues) // Set the content values
                .build() // Build

            recording = videoCapture.output // Get the output as a recorder
                .prepareRecording(context, mediaStoreOutputOptions) // Prepare recording
                .apply { // Apply
                    if (PermissionChecker.checkSelfPermission(context, // If permission checker
                            Manifest.permission.RECORD_AUDIO) == // Get the permission
                        PermissionChecker.PERMISSION_GRANTED) // If permission granted
                    {
                        withAudioEnabled() // With audio enabled
                    }
                }
                .start(ContextCompat.getMainExecutor(context)) { recordEvent -> // Start with main executor
                    when (recordEvent) { // When record event
                        is VideoRecordEvent.Start -> { // If start
                            isRecording.apply { // Apply is recording
                                isRecording = true // Set is recording to true
                            }
                        }
                        is VideoRecordEvent.Finalize -> { // If finalize
                            if(!recordEvent.hasError()) { // If no error
                                val msg = "Video saved: ${recordEvent.outputResults.outputUri}" // Get the message
                                videoFilePath = recordEvent.outputResults.outputUri.toString() // Set the video file path
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT) // Show a toast
                                    .show() // Show
                                Log.d(TAG, msg) // Log message
                            } else { // If error
                                recording?.close() // Close recording
                                recording = null // Set recording to null
                                Log.e(TAG, "Recording error") // Log error
                            }
                        }
                    }
                }
            isRecording = true // Set is recording to true
        }
    }

    // Stop the current recording
    fun stopRecording(){
        Log.w("String","stopRecording")
        if(isRecording){ // If recording
            recording?.stop() // Stop recording
            recording = null // Set recording to null
            isRecording = false // Set is recording to false
        }
    }

    // Get the video file path
    fun getVideoFilePath(): String? {
        return videoFilePath
    }

    // Get if recording
    fun getIsRecording(): Boolean {
        Log.w("String","isRecording: $isRecording")
        return isRecording
    }

    // Constants
    companion object {
        private const val TAG = "VideoActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    // On destroy
    fun onDestroy() {
        cameraExecutor.shutdown()
    }
}

// Luminosity analyzer
    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        listener(luma)

        image.close()
    }
}