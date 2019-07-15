package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.graphics.drawable.Drawable
import java.io.IOException


object FlagDrawablesManager {

    private val flagDrawables: MutableMap<String, Drawable> = mutableMapOf()

    fun getDrawableFor(context: Context, countryCode: Short): Drawable? {
        val imgName = "flags/flag_$countryCode.png"

        return if (!flagDrawables.containsKey(imgName)) {
            loadDrawableFromAssets(context, imgName)?.run {
                flagDrawables[imgName] = this
                this
            }
        } else
            flagDrawables[imgName]
    }

    private fun loadDrawableFromAssets(ctx: Context, path: String): Drawable? {
        try {
            return Drawable.createFromStream(ctx.assets.open(path), null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}