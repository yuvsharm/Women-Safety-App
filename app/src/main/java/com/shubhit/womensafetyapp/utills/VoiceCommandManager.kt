package com.shubhit.womensafetyapp.utills

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceCommandManager(
    private val context: Context,
    private val onCommandReceived: (String) -> Unit,
    private val stopBlinking: () -> Unit
) : RecognitionListener {

    override fun onReadyForSpeech(params: Bundle?) {
        // Handle readiness for speech
    }

    override fun onBeginningOfSpeech() {
        // Handle the beginning of speech
        Log.d("VoiceCommandManager", "Speech started")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Handle changes in the volume of speech
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Handle buffer received
    }

    override fun onEndOfSpeech() {
        // Handle the end of speech
        Log.d("VoiceCommandManager", "Speech ended")
        stopBlinking()
    }

    override fun onError(error: Int) {
        // Handle errors
        Log.e("VoiceCommandManager", "Error: $error")
    }

    override fun onResults(results: Bundle?) {
        // Handle speech recognition results
        Log.d("VoiceCommandManager", "Speech results received")
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val command = matches[0]
            onCommandReceived(command) // Use the lambda to handle the command
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Handle partial results
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Handle other events
    }
}
