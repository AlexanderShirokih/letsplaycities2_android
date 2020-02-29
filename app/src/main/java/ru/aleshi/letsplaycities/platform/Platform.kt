package ru.aleshi.letsplaycities.platform

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.FileProvider
import ru.aleshi.letsplaycities.Localization
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.dictionary.ExclusionsServiceImpl
import ru.aleshi.letsplaycities.base.scoring.CityStatDatabaseHelper
import ru.aleshi.letsplaycities.base.scoring.ScoreManager
import javax.inject.Singleton

/**
 * Android platform-dependent injections
 */
@Module(includes = [Platform.Bindings::class])
class Platform {

    @Module
    interface Bindings {

        @Binds
        fun fileProvider(assetFileProvider: AssetFileProvider): FileProvider

        @Binds
        fun prefs(prefs: SharedGamePreferences): GamePreferences

        @Binds
        fun dbHelper(cityStatDatabaseHelper: SQLiteCityStatDatabaseHelper): CityStatDatabaseHelper
    }

    @Provides
    @Singleton
    fun sharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences("letsplaycities", Context.MODE_PRIVATE)

    @Provides
    @Localization("exclusion-errors")
    fun exclusionsErrMessages(context: Context): Map<ExclusionsServiceImpl.ErrorCode, String> =
        mapOf(
            ExclusionsServiceImpl.ErrorCode.THIS_IS_A_COUNTRY to context.getString(R.string.this_is_a_country),
            ExclusionsServiceImpl.ErrorCode.THIS_IS_A_STATE to context.getString(R.string.this_is_a_state),
            ExclusionsServiceImpl.ErrorCode.RENAMED_CITY to context.getString(R.string.renamed_city),
            ExclusionsServiceImpl.ErrorCode.INCOMPLETE_CITY to context.getString(R.string.uncompleted_city),
            ExclusionsServiceImpl.ErrorCode.NOT_A_CITY to context.getString(R.string.not_city),
            ExclusionsServiceImpl.ErrorCode.THIS_IS_NOT_A_CITY to context.getString(R.string.this_is_not_a_city)
        )

    @Provides
    @Localization("score_result_names")
    fun scoreManagerResults(context: Context): Map<ScoreManager.GameResult, String> =
        mapOf(
            ScoreManager.GameResult.TIME_UP to context.getString(R.string.timeup),
            ScoreManager.GameResult.WIN_BY_REMOTE to context.getString(R.string.win_by_remote),
            ScoreManager.GameResult.WIN to context.getString(R.string.win),
            ScoreManager.GameResult.DRAW to context.getString(R.string.draw)
        )

    @Provides
    @Localization("androidPlayerName")
    fun androidPlayerName(context: Context) = context.getString(R.string.android)

    @Provides
    @Localization("playerName")
    fun playerName(context: Context) = context.getString(R.string.player)
}