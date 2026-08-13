package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ProtocolChapter
import com.example.data.model.ProtocolEntry
import com.example.data.repository.ProtocolRepository
import com.example.util.AudioPlayerManager
import com.example.util.AudioRecorderManager
import com.example.util.ExportShareManager
import com.example.util.ZeroLogikAnalyzer
import com.example.util.ZeroLogikResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProtocolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProtocolRepository
    val audioRecorder: AudioRecorderManager
    val audioPlayer: AudioPlayerManager

    init {
        val dao = AppDatabase.getInstance(application).protocolDao()
        repository = ProtocolRepository(dao)
        audioRecorder = AudioRecorderManager(application)
        audioPlayer = AudioPlayerManager(application)
    }

    // Filter states
    private val _selectedChapter = MutableStateFlow<ProtocolChapter?>(null)
    val selectedChapter: StateFlow<ProtocolChapter?> = _selectedChapter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDateFilter = MutableStateFlow<String?>(null)
    val selectedDateFilter: StateFlow<String?> = _selectedDateFilter

    // Captured Web Clip buffer for easy import into a new entry
    private val _webClipBuffer = MutableStateFlow<Pair<String, String>?>(null) // Pair(url, title)
    val webClipBuffer: StateFlow<Pair<String, String>?> = _webClipBuffer

    // All entries with reactive query filtering
    @OptIn(ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<ProtocolEntry>> = combine(
        _selectedChapter,
        _searchQuery,
        _selectedDateFilter
    ) { chapter, query, date ->
        Triple(chapter, query, date)
    }.flatMapLatest { (chapter, query, date) ->
        when {
            query.isNotBlank() -> repository.searchEntries(query)
            date != null -> repository.getEntriesForDate(date)
            chapter != null -> repository.getEntriesForChapter(chapter.id)
            else -> repository.allEntries
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setChapterFilter(chapter: ProtocolChapter?) {
        _selectedChapter.value = chapter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateFilter(dateStr: String?) {
        _selectedDateFilter.value = dateStr
    }

    fun setWebClipBuffer(url: String, title: String) {
        _webClipBuffer.value = Pair(url, title)
    }

    fun clearWebClipBuffer() {
        _webClipBuffer.value = null
    }

    fun saveEntry(
        id: Long = 0,
        title: String,
        content: String,
        chapter: ProtocolChapter,
        audioPath: String? = null,
        audioDurationMs: Long = 0,
        imageUri: String? = null,
        documentUri: String? = null,
        documentName: String? = null,
        webUrl: String? = null,
        webTitle: String? = null,
        isPublic: Boolean = false
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val analyzerResult = ZeroLogikAnalyzer.analyze(content)

            val entry = ProtocolEntry(
                id = id,
                dateStr = dateStr,
                timestamp = System.currentTimeMillis(),
                title = if (title.isBlank()) "${chapter.title} - ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}" else title,
                content = content,
                chapter = chapter.id,
                audioPath = audioPath,
                audioDurationMs = audioDurationMs,
                imageUri = imageUri,
                documentUri = documentUri,
                documentName = documentName,
                webUrl = webUrl,
                webTitle = webTitle,
                coherenceScore = analyzerResult.coherenceScore,
                isPublicProtocol = isPublic
            )

            if (id == 0L) {
                repository.insertEntry(entry)
            } else {
                repository.updateEntry(entry)
            }
        }
    }

    fun deleteEntry(entry: ProtocolEntry) {
        viewModelScope.launch {
            // Remove audio file if created by app
            entry.audioPath?.let { path ->
                try {
                    File(path).delete()
                } catch (e: Exception) {
                    // Ignore deletion error
                }
            }
            repository.deleteEntry(entry)
        }
    }

    fun exportAll(format: ExportShareManager.ExportFormat) {
        val currentEntries = entries.value
        ExportShareManager.exportAndShareEntries(
            getApplication(),
            currentEntries,
            format,
            "Protokoll_0_Export"
        )
    }

    fun readTextFromUri(uri: Uri): String {
        return try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            inputStream?.bufferedReader()?.use { it.readText() } ?: ""
        } catch (e: Exception) {
            "Fehler beim Lesen der Datei: ${e.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        audioRecorder.cancelRecording()
    }
}
