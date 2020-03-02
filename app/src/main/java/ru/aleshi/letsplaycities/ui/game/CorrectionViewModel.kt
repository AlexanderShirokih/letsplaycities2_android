package ru.aleshi.letsplaycities.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import ru.aleshi.letsplaycities.base.game.WordCheckingResult

/**
 * Used to pass [corrections] from GameViewModel to CorrectionTipsDialog
 */
class CorrectionViewModel : ViewModel() {

    val corrections = MediatorLiveData<List<String>>()

    lateinit var correctionCallback: (city: String) -> Unit

    fun setCorrectionsList(
        wordState: LiveData<WordCheckingResult>,
        correctionCallback: (city: String) -> Unit
    ) {
        this.correctionCallback = correctionCallback
        this.corrections.addSource(wordState) {
            when (it) {
                is WordCheckingResult.Corrections -> this.corrections.value = it.corrections
                else -> this.corrections.value = emptyList()
            }
        }
    }

    /**
     * Propagates corrected city to [correctionCallback]
     */
    fun processCityInput(city: String) {
        correctionCallback(city)
    }

}