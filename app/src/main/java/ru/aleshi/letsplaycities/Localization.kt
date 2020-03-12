package ru.aleshi.letsplaycities

import javax.inject.Qualifier

/**
 * Used to mark localization resources.
 * @param name name key
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Localization(val name: String)