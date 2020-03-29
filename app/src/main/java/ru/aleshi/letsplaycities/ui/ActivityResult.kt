package ru.aleshi.letsplaycities.ui

import android.content.Intent

/**
 * Wraps the parameters of onActivityResult
 *
 * @property resultCode the result code returned from the activity.
 * @property data the optional intent returned from the activity.
 */
data class ActivityResult(
    val resultCode: Int,
    val data: Intent?
)