package ru.aleshi.letsplaycities.base.game

import ru.aleshi.letsplaycities.base.player.User

/**
 * Reasons why game has finished.
 * @param target [User] who initiates this event
 * @param reason finish reason
 */
class FinishEvent(val target: User, val reason: Reason) {
    enum class Reason {
        TimeOut, Surrender, Disconnected, Kicked
    }
}