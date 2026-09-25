package com.aera.robot.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var onSpeakingFinished: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
                // Fallback to default if Indonesian not available
                if (tts?.isLanguageAvailable(Locale("id", "ID")) == TextToSpeech.LANG_MISSING_DATA ||
                    tts?.isLanguageAvailable(Locale("id", "ID")) == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts?.language = Locale.getDefault()
                }
                isReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        onSpeakingFinished?.invoke()
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onSpeakingFinished?.invoke()
                    }
                })
            }
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!isReady || tts == null) {
            onFinished?.invoke()
            return
        }
        onSpeakingFinished = onFinished
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aera_utterance")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
