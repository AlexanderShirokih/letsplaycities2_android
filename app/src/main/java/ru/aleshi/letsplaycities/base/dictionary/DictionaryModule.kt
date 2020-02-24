package ru.aleshi.letsplaycities.base.dictionary

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import ru.aleshi.letsplaycities.GsonModule
import ru.aleshi.letsplaycities.base.GamePreferences

@Module(includes = [GsonModule::class])
class DictionaryModule {

    @Provides
    fun dictionaryUpdater(
        gson: Gson,
        prefs: GamePreferences,
        factory: DictionaryFactory
    ): DictionaryUpdater = DictionaryUpdater(gson, prefs, factory)

    @Provides
    fun exclusions(factory: ExclusionsFactory): Single<ExclusionsService> = factory.load()

    @Provides
    fun dictionary(factory: DictionaryFactory): Single<DictionaryService> = factory.load()

}