package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.audio.SpeechToTextHelper
import com.example.util.audio.SpeechToTextState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SpeechToTextHelperTest {

    private lateinit var context: Context
    private lateinit var speechHelper: SpeechToTextHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        speechHelper = SpeechToTextHelper(context)
    }

    @Test
    fun `test initial state is Idle`() {
        assertTrue("Speech helper initial state must be Idle", speechHelper.state.value is SpeechToTextState.Idle)
        assertEquals("Initial transcribed text must be empty", "", speechHelper.transcribedText.value)
    }

    @Test
    fun `test state transitions and result handling`() {
        val partialState = SpeechToTextState.PartialResult("Hallo Welt")
        val finalState = SpeechToTextState.FinalResult("Hallo Welt fertig")
        val errorState = SpeechToTextState.Error("Netzwerkfehler", 2)

        assertEquals("Hallo Welt", partialState.partialText)
        assertEquals("Hallo Welt fertig", finalState.text)
        assertEquals("Netzwerkfehler", errorState.message)
        assertEquals(2, errorState.errorCode)
    }

    @Test
    fun `test clearTranscribedText resets buffer and state`() {
        speechHelper.clearTranscribedText()
        assertEquals("", speechHelper.transcribedText.value)
        assertTrue(speechHelper.state.value is SpeechToTextState.Idle)
    }
}
