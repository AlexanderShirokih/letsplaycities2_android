package ru.aleshi.letsplaycities

/**
 * Wrapper class which has one of two states:
 * [Success] - when value is present
 * [Failure] - when some error happen
 */
sealed class Result<T : Any> {

    companion object {
        fun <T : Any> success(value: T): Result<T> = Success(value)

        fun <T : Any> failure(error: Throwable): Result<T> = Failure(error)
    }

    /**
     * Success value state.
     * @param value the value for holding
     */
    class Success<T : Any>(val value: T) : Result<T>()

    /**
     * Failure result
     * @param error the error for holding
     */
    class Failure<T : Any>(val error: Throwable) : Result<T>()

    /**
     * Returns value when result is success or `null` when result if failure.
     */
    val valueOrNull: T? get() = if (this is Success) value else null

}