package ru.aleshi.letsplaycities.platform

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.FileProvider
import ru.aleshi.letsplaycities.Localization
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.dictionary.ExclusionsServiceImpl

/**
 * Android platform-dependent injections
 */
@Module(includes = [Platform.Bindings::class])
class Platform {

    @Module
    interface Bindings {

        @Binds
        fun fileProvider(assetFileProvider: AssetFileProvider): FileProvider

    }

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

}