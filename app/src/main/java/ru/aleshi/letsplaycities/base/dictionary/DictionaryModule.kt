package ru.aleshi.letsplaycities.base.dictionary

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import ru.aleshi.letsplaycities.GsonModule

@Module(includes = [GsonModule::class])
class DictionaryModule {

    @Provides
    fun dictionaryUpdater(gson: Gson): DictionaryUpdater = DictionaryUpdater(gson)

}