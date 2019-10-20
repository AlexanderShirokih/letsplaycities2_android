package ru.aleshi.letsplaycities

import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import javax.inject.Inject

class LPSApplication : MultiDexApplication(), HasAndroidInjector {

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
        Crashlytics.getInstance()
    }
}