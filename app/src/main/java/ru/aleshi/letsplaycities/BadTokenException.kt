package ru.aleshi.letsplaycities

/**
 * Created by Alexander Shirokih on 09.06.18.
 */

class BadTokenException(val description: String) : RuntimeException(description) {
    constructor() : this("Invalid token!")
}
