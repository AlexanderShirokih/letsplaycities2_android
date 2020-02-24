package ru.aleshi.letsplaycities.base.game

import ru.aleshi.letsplaycities.ui.game.CityStatus

/**
 * Represents model of game item (city or message).
 */
sealed class GameEntity {

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
    ) : GameEntity()

    /**
     * Info containing data about message.
     * @param message Message content
     * @param position on the screen
     */
    data class MessageInfo(
        val message: String,
        val position: Position
    ) : GameEntity()
}