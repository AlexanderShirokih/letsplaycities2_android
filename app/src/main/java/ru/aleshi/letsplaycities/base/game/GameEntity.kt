package ru.aleshi.letsplaycities.base.game

import ru.aleshi.letsplaycities.base.player.UserIdentity
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.aleshi.letsplaycities.ui.game.CityStatus
import ru.quandastudio.lpsclient.model.WordResult

/**
 * Represents model of game item (city or message).
 */
sealed class GameEntity {

    /**
     * Tests that this entity has the same content(city or message) with [entity].
     */
    fun areTheSameWith(entity: GameEntity): Boolean {
        return when {
            entity is CityInfo && this is CityInfo -> entity.city == city
            entity is MessageInfo && this is MessageInfo -> entity.message == message
            else -> false
        }
    }

    /**
     * Info containing data about city.
     * @param city City
     * @param position position on the screen
     * @param countryCode code of cities country in database
     * @param status current status of the city (waiting for confirmation, error, accepted)
     */
    data class CityInfo(
        val city: String,
        val position: Position,
        val countryCode: Short = 0,
        val status: CityStatus
    ) : GameEntity() {

        constructor(
            result: ResultWithCity,
            countryCodeProvider: (city: String) -> Short,
            positionProvider: (identity: UserIdentity) -> Position
        ) : this(
            city = result.city,
            countryCode = countryCodeProvider(result.city),
            position = positionProvider(result.identity),
            status = when (result.wordResult) {
                WordResult.ACCEPTED, WordResult.RECEIVED -> CityStatus.OK
                WordResult.UNKNOWN -> CityStatus.WAITING
                else -> CityStatus.ERROR
            }
        )
    }

    /**
     * Info containing data about message.
     * @param message Message content
     * @param position on the screen
     */
    data class MessageInfo(
        val message: String,
        val position: Position
    ) : GameEntity() {

        constructor(
            result: ResultWithMessage,
            positionProvider: (identity: UserIdentity) -> Position
        ) : this(
            message = result.message,
            position = positionProvider(result.identity)
        )
    }
}