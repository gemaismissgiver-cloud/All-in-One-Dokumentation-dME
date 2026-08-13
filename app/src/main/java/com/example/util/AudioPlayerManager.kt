package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    fun playOrPause(path: String) {
        if (_currentPath.value == path && mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) {
                pause()
            } else {
                resume()
            }
            return
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                if (path.startsWith("content://")) {
                    setDataSource(context, Uri.parse(path))
                } else {
                    val file = File(path)
                    if (!file.exists()) {
                        Log.e("AudioPlayer", "File does not exist: $path")
                        return
                    }
                    setDataSource(file.absolutePath)
                }
                prepare()
                start()
            }

            mediaPlayer = player
            _currentPath.value = path
            _duration.value = player.duration
            _isPlaying.value = true

            player.setOnCompletionListener {
                _isPlaying.value = false
                _currentPosition.value = 0
                stopProgressJob()
            }

            startProgressJob()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio", e)
            stop()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        stopProgressJob()
    }

    fun resume() {
        mediaPlayer?.start()
        _isPlaying.value = true
        startProgressJob()
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping player", e)
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            _currentPath.value = null
            _currentPosition.value = 0
            _duration.value = 0
        }
    }

    private fun startProgressJob() {
        stopProgressJob()
        progressJob = scope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopProgressJob() {
        progressJob?.cancel()
        progressJob = null
    }
}
