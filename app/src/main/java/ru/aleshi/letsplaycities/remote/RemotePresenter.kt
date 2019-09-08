package ru.aleshi.letsplaycities.remote

import ru.quandastudio.lpsclient.model.PlayerData
import javax.inject.Inject

class RemotePresenter @Inject constructor() : RemoteContract.Presenter {

    lateinit var view: RemoteContract.View


    override fun onCreateConnection() {

    }

}