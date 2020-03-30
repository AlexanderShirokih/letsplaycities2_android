package ru.aleshi.letsplaycities.social

/**
 * Provides access to service that manages achievements and score manager
 */
interface AchievementService {

    /**
     * Unlocks or increments achievement
     * @param achievement the achievement to be unlocked
     * @param incrementCount how much increment achievement, only for incremental achievements
     */
    fun unlockAchievement(achievement: Achievement, incrementCount: Int = 1)

    /**
     * Submits score to play games server only if user logged in
     * @param score user score to be submitted
     */
    fun submitScore(score: Int)

}