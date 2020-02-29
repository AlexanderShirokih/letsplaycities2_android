package ru.aleshi.letsplaycities.base.game

/**
 * [WordCheckingResult] describes states when [ru.aleshi.letsplaycities.base.player.User] should
 * return after receiving input from keyboard.
 */
sealed class WordCheckingResult {

    /**
     * Used when input city has already used.
     */
    object AlreadyUsed : WordCheckingResult()

    /**
     * Used when input city is an exclusion and can't be applied.
     */
    class Exclusion(val description: String) : WordCheckingResult()

    /**
     * Used when input [word] not found in the database.
     */
    class OriginalNotFound(val word: String) : WordCheckingResult()

    /**
     * Used after state [OriginalNotFound] and contains available corrections for current input
     */
    class Corrections(val corrections: List<String>) : WordCheckingResult()

    /**
     * Used after state [OriginalNotFound] when no corrections available.
     */
    object NotFound : WordCheckingResult()

    /**
     * Used when input [word] can applied without any corrections.
     * Note that [word] can be formatted to proper format.
     */
    class Accepted(val word: String) : WordCheckingResult()
}