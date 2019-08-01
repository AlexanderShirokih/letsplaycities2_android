package ru.aleshi.letsplaycities.base.dictionary

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides

@Module
class DictionaryModule {

    @Provides
    fun dictionaryUpdater(gson: Gson): DictionaryUpdater = DictionaryUpdater(gson)

    @Provides
    fun gson(): Gson = GsonBuilder()
        .create()
}