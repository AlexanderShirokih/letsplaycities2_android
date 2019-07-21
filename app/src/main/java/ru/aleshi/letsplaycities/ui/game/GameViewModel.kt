package ru.aleshi.letsplaycities.ui.game

import android.graphics.drawable.Drawable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    val avatarLeft: ObservableField<Drawable> = ObservableField()
    val infoLeft: ObservableField<String> = ObservableField()
    
    var avatarRight: ObservableField<Drawable> = ObservableField()
    val infoRight: ObservableField<String> = ObservableField()

    val isLeftActive: ObservableBoolean = ObservableBoolean()

    var timer: ObservableField<String> = ObservableField()
}