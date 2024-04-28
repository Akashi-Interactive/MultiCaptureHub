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
        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(Runnable{
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer)


            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))

    }

    fun startRecording(){
        val videoCapture = this.videoCapture ?: return

        if(!isRecording){
            val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VideoHub")
                }
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions
                .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build()

            recording = videoCapture.output
                .prepareRecording(context, mediaStoreOutputOptions)
                .apply {
                    if (PermissionChecker.checkSelfPermission(context,
                            Manifest.permission.RECORD_AUDIO) ==
                        PermissionChecker.PERMISSION_GRANTED)
                    {
                        withAudioEnabled()
                    }
                }
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            isRecording.apply {
                                isRecording = true
                            }
                        }
                        is VideoRecordEvent.Finalize -> {
                            if(!recordEvent.hasError()) {
                                val msg = "Video saved: ${recordEvent.outputResults.outputUri}"
                                videoFilePath = recordEvent.outputResults.outputUri.toString()
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                                    .show()
                                Log.d(TAG, msg)
                            } else {
                                recording?.close()
                                recording = null
                                Log.e(TAG, "Recording error")
                            }
                        }
                    }
                }
        }
    }

    // Stop the current recording
    fun stopRecording(){
        if(isRecording){
            recording?.stop()
            recording = null
            isRecording = false
        }
    }

    fun getVideoFilePath(): String? {
        return videoFilePath
    }

    fun getIsRecording(): Boolean {
        return isRecording
    }

    companion object {
        private const val TAG = "VideoActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    fun onDestroy() {
        cameraExecutor.shutdown()
    }
}
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