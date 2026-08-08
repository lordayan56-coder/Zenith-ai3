package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var speechRecognizer: SpeechRecognizer? = null

    val isListening = MutableStateFlow(false)
    val isSpeaking = MutableStateFlow(false)
    val audioAmplitude = MutableStateFlow(0f)
    val recognizedText = MutableStateFlow("")
    val isTtsReady = MutableStateFlow(false)
    val lastError = MutableStateFlow<String?>(null)

    init {
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            // Pitch 0.85 for deep, natural adult male voice tone
            tts?.setPitch(0.85f)
            tts?.setSpeechRate(1.0f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking.value = true
                    audioAmplitude.value = 0.6f
                }

                override fun onDone(utteranceId: String?) {
                    isSpeaking.value = false
                    audioAmplitude.value = 0f
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isSpeaking.value = false
                    audioAmplitude.value = 0f
                }
            })
            isTtsReady.value = true
        } else {
            lastError.value = "TTS Initialization Failed"
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening.value = true
                        lastError.value = null
                    }

                    override fun onBeginningOfSpeech() {
                        audioAmplitude.value = 0.4f
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB (0..10 typical) to 0.0..1.0
                        audioAmplitude.value = (rmsdB / 12f).coerceIn(0f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening.value = false
                        audioAmplitude.value = 0f
                    }

                    override fun onError(error: Int) {
                        isListening.value = false
                        audioAmplitude.value = 0f
                        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            lastError.value = "Speech Recognition Error Code $error"
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListening.value = false
                        audioAmplitude.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            recognizedText.value = matches[0]
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            recognizedText.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        // Stop any ongoing speech output if user interrupts
        stopSpeaking()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening.value = true
        } catch (e: Exception) {
            lastError.value = "Cannot start mic: ${e.message}"
            isListening.value = false
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening.value = false
        audioAmplitude.value = 0f
    }

    fun speak(text: String, utteranceId: String = "ZENITH_SPEECH") {
        stopListening()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        isSpeaking.value = false
        audioAmplitude.value = 0f
    }

    fun destroy() {
        speechRecognizer?.destroy()
        tts?.shutdown()
    }
}
