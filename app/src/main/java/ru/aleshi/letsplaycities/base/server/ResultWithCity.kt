package ru.aleshi.letsplaycities.base.server

import ru.aleshi.letsplaycities.base.player.UserIdentity
import ru.quandastudio.lpsclient.model.WordResult

/**
 * Wrapper class for [WordResult]
 * @param wordResult [WordResult]
 * @param city city
 * @param identity owner of this word
 */
data class ResultWithCity(
    val wordResult: WordResult,
    val city: String,
    val identity: UserIdentity
) {
    fun isSuccessful() = wordResult == WordResult.ACCEPTED || wordResult == WordResult.RECEIVED
}