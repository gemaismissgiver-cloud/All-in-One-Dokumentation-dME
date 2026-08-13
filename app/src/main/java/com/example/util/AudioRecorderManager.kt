package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorderManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    var currentOutputFile: File? = null
        private set
    var isRecording: Boolean = false
        private set
    private var startTimeMs: Long = 0

    fun startRecording(): Boolean {
        try {
            val audioDir = File(context.filesDir, "audio_protocols")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(audioDir, "PROTOCOL_REC_$timestamp.m4a")
            currentOutputFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            startTimeMs = System.currentTimeMillis()
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error starting recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            currentOutputFile = null
            return false
        }
    }

    fun stopRecording(): Pair<File?, Long> {
        if (!isRecording) return Pair(null, 0L)

        val duration = System.currentTimeMillis() - startTimeMs
        val file = currentOutputFile

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recorder", e)
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }

        return Pair(file, duration)
    }

    fun cancelRecording() {
        try {
            if (isRecording) {
                mediaRecorder?.stop()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error canceling recording", e)
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }
}
