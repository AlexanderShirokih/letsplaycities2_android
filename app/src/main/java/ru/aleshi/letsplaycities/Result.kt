package ru.aleshi.letsplaycities

sealed class Result<T : Any> {

    companion object {
        fun <T : Any> success(value: T): Result<T> = Success(value)

        fun <T : Any> failure(error: Throwable): Result<T> = Failure(error)
    }

    class Success<T : Any>(val value: T) : Result<T>()

    class Failure<T : Any>(val error: Throwable) : Result<T>()

}