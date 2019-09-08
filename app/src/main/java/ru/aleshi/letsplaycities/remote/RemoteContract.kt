package ru.aleshi.letsplaycities.remote


interface RemoteContract {
    interface View {
    }

    interface Presenter {
        fun onCreateConnection()
    }
}