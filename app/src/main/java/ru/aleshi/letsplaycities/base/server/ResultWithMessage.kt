package ru.aleshi.letsplaycities.base.server

import ru.aleshi.letsplaycities.base.player.UserIdentity

/**
 * Wrapper class for incoming message
 * @param message incoming message
 * @param identity owner of this message
 */
data class ResultWithMessage(val message: String, val identity: UserIdentity)
