package ru.aleshi.letsplaycities.ui.network

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AvatarViewModel : ViewModel() {

    val avatarPath: MutableLiveData<String> = MutableLiveData()
}