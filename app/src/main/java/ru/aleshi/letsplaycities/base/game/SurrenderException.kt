package ru.aleshi.letsplaycities.base.game

import ru.aleshi.letsplaycities.base.player.User

/**
 * Throws when [ru.aleshi.letsplaycities.base.player.User] surrenders.
 */
class SurrenderException(val target: User, val byDisconnection: Boolean) : Exception()