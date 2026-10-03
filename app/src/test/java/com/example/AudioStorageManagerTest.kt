package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.audio.AudioRecordItem
import com.example.util.audio.AudioRecordingState
import com.example.util.audio.AudioStorageManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AudioStorageManagerTest {

    private lateinit var context: Context
    private lateinit var storageManager: AudioStorageManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageManager = AudioStorageManager(context)
        // Clean up test recordings
        storageManager.clearAllRecordings("test_recordings")
    }

    @Test
    fun `test recordings directory is created in internal storage`() {
        val dir = storageManager.getRecordingsDirectory("test_recordings")
        assertTrue("Directory should exist", dir.exists())
        assertTrue("Path should be directory", dir.isDirectory)
        assertTrue("Path should be in internal filesDir", dir.absolutePath.startsWith(context.filesDir.absolutePath))
    }

    @Test
    fun `test createOutputFile creates unique file with m4a extension`() {
        val file1 = storageManager.createOutputFile(prefix = "TEST", extension = "m4a", subdir = "test_recordings")
        val file2 = storageManager.createOutputFile(prefix = "TEST", extension = "m4a", subdir = "test_recordings")

        assertTrue("File should end with .m4a", file1.name.endsWith(".m4a"))
        assertTrue("File should start with prefix TEST", file1.name.startsWith("TEST_"))
        assertNotEquals("Files created in sequence should have unique names", file1.name, file2.name)
    }

    @Test
    fun `test listRecordings and deleteRecording`() {
        val file = storageManager.createOutputFile(prefix = "SAMPLE", extension = "m4a", subdir = "test_recordings")
        file.writeBytes(byteArrayOf(0, 1, 2, 3, 4)) // simulate small audio file

        val list = storageManager.listRecordings("test_recordings")
        assertEquals("Should have 1 recorded file", 1, list.size)
        assertEquals("File name should match", file.name, list[0].fileName)
        assertEquals("File size should match 5 bytes", 5L, list[0].sizeBytes)

        val deleted = storageManager.deleteRecording(file)
        assertTrue("File deletion should succeed", deleted)
        assertFalse("File should no longer exist", file.exists())

        val listAfter = storageManager.listRecordings("test_recordings")
        assertEquals("Should have 0 recorded files after deletion", 0, listAfter.size)
    }

    @Test
    fun `test formatDuration formats time correctly`() {
        assertEquals("00:00", storageManager.formatDuration(0L))
        assertEquals("00:05", storageManager.formatDuration(5000L))
        assertEquals("01:30", storageManager.formatDuration(90000L))
        assertEquals("01:00:00", storageManager.formatDuration(3600000L))
    }

    @Test
    fun `test formatFileSize formats bytes accurately`() {
        assertEquals("500 B", storageManager.formatFileSize(500L))
        val kbFormatted = storageManager.formatFileSize(2048L)
        assertTrue("Should contain KB", kbFormatted.contains("KB"))
    }

    @Test
    fun `test AudioRecordingState transitions`() {
        val dummyFile = File(context.filesDir, "dummy.m4a")
        val idleState: AudioRecordingState = AudioRecordingState.Idle
        val recordingState: AudioRecordingState = AudioRecordingState.Recording(dummyFile, 1200L)
        val pausedState: AudioRecordingState = AudioRecordingState.Paused(dummyFile, 1200L)
        val stoppedState: AudioRecordingState = AudioRecordingState.Stopped(dummyFile, 5000L)

        assertTrue(idleState is AudioRecordingState.Idle)
        assertEquals(dummyFile, (recordingState as AudioRecordingState.Recording).file)
        assertEquals(1200L, (pausedState as AudioRecordingState.Paused).elapsedMs)
        assertEquals(5000L, (stoppedState as AudioRecordingState.Stopped).durationMs)
    }
}
