package ru.aleshi.letsplaycities.social

/**
 * Provides access to service that manages achievements and score manager
 */
interface AchievementService {

    /**
     * Unlocks or increments achievement
     * @param achievement the achievement to be unlocked
     * @return `true` if user signed in to service or `false` if not
     */
    suspend fun unlockAchievement(achievement: Achievement): Boolean

    /**
     * Submits score to play games server only if user logged in
     * @param score user score to be submitted
     */
    suspend fun submitScore(score: Int)

}