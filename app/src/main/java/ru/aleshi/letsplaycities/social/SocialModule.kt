package ru.aleshi.letsplaycities.social

import dagger.Binds
import dagger.Module

@Module
interface SocialModule {

    @Binds
    fun achievementService(googleAchievementService: GoogleGameServicesHelper): AchievementService

}