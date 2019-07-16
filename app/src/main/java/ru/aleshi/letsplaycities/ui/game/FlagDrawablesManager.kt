package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ru.aleshi.letsplaycities.R
import java.io.IOException
import kotlin.math.roundToInt


object FlagDrawablesManager {

    private val flagDrawables: MutableMap<String, Bitmap> = mutableMapOf()

    fun getBitmapFor(context: Context, countryCode: Short): Bitmap? {
        val imgName = "flags/flag_$countryCode.png"

        return if (!flagDrawables.containsKey(imgName)) {
            loadBitmapFromAssets(context, imgName)?.run {
                flagDrawables[imgName] = this
                this
            }
        } else
            flagDrawables[imgName]
    }

    private fun loadBitmapFromAssets(ctx: Context, path: String): Bitmap? {
        return try {
            val width = ctx.resources.getDimension(R.dimen.flagImgWidth).roundToInt()
            val height = ctx.resources.getDimension(R.dimen.flagImgHeight).roundToInt()
            Bitmap.createScaledBitmap(BitmapFactory.decodeStream(ctx.assets.open(path)), width, height, true)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}