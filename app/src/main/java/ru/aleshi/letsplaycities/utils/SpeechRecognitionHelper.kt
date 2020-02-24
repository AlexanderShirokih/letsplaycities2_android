package ru.aleshi.letsplaycities.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import ru.aleshi.letsplaycities.R

object SpeechRecognitionHelper {

    private const val VOICE_RECOGNITION_REQUEST_CODE = 22

    @MainThread
    fun speech(fragment: Fragment, a: Activity) {
        val pm = a.packageManager
        val activities = pm.queryIntentActivities(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
        if (activities.isEmpty()) {
            Toast.makeText(a, R.string.no_speech_app, Toast.LENGTH_LONG).show()
        } else {
            fragment.startActivityForResult(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_PROMPT, a.getString(R.string.tell_city))
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf<String>())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }, VOICE_RECOGNITION_REQUEST_CODE)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, onResult: (result: String) -> Unit) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as List<String>
            if (matches.isNotEmpty()) {
                onResult(matches[0])
            }
        }
    }
}