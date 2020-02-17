package ru.aleshi.letsplaycities.ui

sealed class FetchState {
    object LoadingState : FetchState()
    data class DataState<T>(val data: T) : FetchState()
    data class ErrorState(val error: Throwable) : FetchState()
    object FinishState : FetchState()
}