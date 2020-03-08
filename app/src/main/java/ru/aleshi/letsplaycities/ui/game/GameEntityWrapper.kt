package ru.aleshi.letsplaycities.ui.game

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import ru.aleshi.letsplaycities.base.game.GameEntity
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.utils.StringUtils
import java.util.*

/**
 * [GameEntity] wrapper for using in ViewHolder
 * @param gameEntity wrapped entity
 */
class GameEntityWrapper(
    internal val gameEntity: GameEntity
) {

    /**
     * Returns `true` if this entity type is [GameEntity.MessageInfo]
     */
    val isMessage: Boolean = gameEntity is GameEntity.MessageInfo

    /**
     * Returns [Position] position
     */
    val position: Position = when (gameEntity) {
        is GameEntity.CityInfo -> gameEntity.position
        is GameEntity.MessageInfo -> gameEntity.position
    }

    /**
     * Returns [CityStatus].
     * For [GameEntity.CityInfo] will return its status, for other types [CityStatus.OK]
     */
    val status: CityStatus = when (gameEntity) {
        is GameEntity.CityInfo -> gameEntity.status
        else -> CityStatus.OK
    }

    /**
     * Returns country code or `0` if its not found.
     */
    val countryCode: Short = when (gameEntity) {
        is GameEntity.CityInfo -> gameEntity.countryCode
        is GameEntity.MessageInfo -> 0
    }

    /**
     * Creates [SpannableStringBuilder] to wrap message or city.
     * For city will highlighted first and last letters.
     */
    fun getSpannableString(foregroundSpanColor: Int): CharSequence {
        return when (gameEntity) {
            is GameEntity.MessageInfo -> SpannableStringBuilder(gameEntity.message)
            is GameEntity.CityInfo -> {
                val city = gameEntity.city
                val end =
                    city.lastIndexOf(StringUtils.findLastSuitableChar(city.toLowerCase(Locale.getDefault())))
                SpannableStringBuilder(StringUtils.toTitleCase(city)).apply {
                    setSpan(
                        ForegroundColorSpan(foregroundSpanColor),
                        0,
                        1,
                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        ForegroundColorSpan(foregroundSpanColor),
                        end,
                        end + 1,
                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }

    /**
     * Returns a string representation of the object.
     */
    override fun toString() = gameEntity.toString()
}