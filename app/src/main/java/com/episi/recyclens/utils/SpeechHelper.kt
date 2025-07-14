package com.episi.recyclens.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var isSpeaking = false
    private var lastSpoken: String? = null

    init {
        tts = TextToSpeech(context.applicationContext) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }

        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
                lastSpoken = null
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
                lastSpoken = null
            }
        })
    }

    fun speakIfPossible(label: String) {
        if (!isSpeaking && lastSpoken != label) {
            lastSpoken = label
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
            tts?.speak(label, TextToSpeech.QUEUE_FLUSH, params, "utteranceId")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isSpeaking = false
        lastSpoken = null
    }
}