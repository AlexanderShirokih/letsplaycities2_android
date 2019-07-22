package ru.aleshi.letsplaycities.base.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.utils.Utils

class NetworkUser(playerData: PlayerData) : User(playerData) {

    override fun getAvatar(context: Context): Maybe<Drawable> {
        return if (playerData.avatar == null) Maybe.empty() else Utils.loadAvatar(context, playerData.avatar!!)
    }

    override fun onBeginMove(firstChar: Char?) {
        // Word broadcasts by NetworkServer
    }
}