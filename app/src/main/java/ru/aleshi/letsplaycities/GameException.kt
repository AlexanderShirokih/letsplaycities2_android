package ru.aleshi.letsplaycities

import java.lang.RuntimeException

/**
 *  Used to internal non critical errors.
 */
class GameException(message: String) : RuntimeException(message)