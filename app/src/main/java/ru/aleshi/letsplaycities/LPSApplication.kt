package ru.aleshi.letsplaycities

import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import javax.inject.Inject

class LPSApplication : Application(), HasAndroidInjector {

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    val gamePreferences: GamePreferences by lazy {
        GamePreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent
            .builder()
            .application(this)
            .build()
            .inject(this)

        SocialNetworkManager.init(this, ServiceType.VK)
    }
}