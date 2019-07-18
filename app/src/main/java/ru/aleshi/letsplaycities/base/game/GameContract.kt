package ru.aleshi.letsplaycities.base.game

import android.content.Context
import android.graphics.drawable.Drawable

interface GameContract {


    interface View {
        fun showInfo(msg: String)
        fun showError(err: Throwable)
        fun updateLabel(info: String, left: Boolean)
        fun updateAvatar(image: Drawable, left: Boolean)
        fun context(): Context
        fun putCity(city: String, countryCode: Short, left: Boolean)
        fun updateCity(city: String, hasErrors: Boolean)
        fun showGameResults(result: String)
    }

    interface Presenter {
        fun onAttachView(view: View)
        fun onDetachView()
        fun submit(userInput: String, callback: () -> Unit): Boolean
        fun useHint()
        fun onSurrender()
    }

}