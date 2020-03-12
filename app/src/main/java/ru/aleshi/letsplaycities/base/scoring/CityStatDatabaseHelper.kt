package ru.aleshi.letsplaycities.base.scoring

import io.reactivex.Completable

/**
 * Helper interface for accessing database
 */
interface CityStatDatabaseHelper {

    /**
     * Updates top 10 frequent words in database
     */
    fun updateFrequentWords(city: String, mostFrequent: ScoringGroup): Completable

}