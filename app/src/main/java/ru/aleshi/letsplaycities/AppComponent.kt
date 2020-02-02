package ru.aleshi.letsplaycities

import com.squareup.picasso.Picasso
import dagger.BindsInstance
import dagger.Component
import ru.aleshi.letsplaycities.base.game.GameModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, GameModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: LPSApplication): Builder

        @BindsInstance
        fun picasso(picasso: Picasso): Builder

        fun build(): AppComponent
    }

    fun inject(app: LPSApplication)
}