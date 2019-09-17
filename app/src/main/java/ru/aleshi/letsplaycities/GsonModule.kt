package ru.aleshi.letsplaycities

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides

@Module
class GsonModule {

    @Provides
    fun gson(): Gson = GsonBuilder()
        .create()

}