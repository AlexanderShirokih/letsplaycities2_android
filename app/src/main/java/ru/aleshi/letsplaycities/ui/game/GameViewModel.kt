package ru.aleshi.letsplaycities.ui.game

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.squareup.picasso.RequestCreator

class GameViewModel : ViewModel() {
    val imageLeft: ObservableField<RequestCreator>  = ObservableField()
    val imageRight: ObservableField<RequestCreator>  = ObservableField()

    val infoLeft: ObservableField<String> = ObservableField()
    val infoRight: ObservableField<String> = ObservableField()

    val isLeftActive: ObservableBoolean = ObservableBoolean()

    var timer: ObservableField<String> = ObservableField()

    val helpBtnVisible : ObservableBoolean = ObservableBoolean()
    val msgBtnVisible : ObservableBoolean = ObservableBoolean()
}