package ru.aleshi.letsplaycities.base.game

/**
 * [WordCheckingResult] describes states when [ru.aleshi.letsplaycities.base.player.User] should
 * return after receiving input from keyboard.
 */
sealed class WordCheckingResult {

    /**
     * Used when input [word] has already used.
     */
    class AlreadyUsed(val word: String) : WordCheckingResult()

    /**
     * Used when input word starts with different letter then [validLetter].
     */
    class WrongLetter(val validLetter: Char) : WordCheckingResult()

    /**
     * Used when input city is an exclusion and can't be applied.
     */
    class Exclusion(val description: String) : WordCheckingResult()

    /**
     * Used after state [NotFound] and contains available corrections for current input
     */
    class Corrections(val corrections: List<String>) : WordCheckingResult()

    /**
     * Used when no corrections available.
     */
    class NotFound(val word: String) : WordCheckingResult()

    /**
     * Used when input [word] can applied without any corrections.
     * Note that [word] can be formatted to proper format.
     */
    class Accepted(val word: String) : WordCheckingResult()
}