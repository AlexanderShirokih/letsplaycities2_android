package ru.aleshi.letsplaycities

import android.content.Context
import android.util.Base64
import android.widget.Toast
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import javax.inject.Inject


class LPSApplication : MultiDexApplication(), HasAndroidInjector {

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var gamePreferences: GamePreferences

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent
            .builder()
            .application(this)
            .picasso(initPicasso(this))
            .build()
            .inject(this)
        SocialNetworkManager.init(this, ServiceType.VK)

        RxJavaPlugins.setErrorHandler {
            Crashlytics.logException(it)
            showErrorToast(it)
        }
    }

    private fun showErrorToast(error: Throwable) {
        val context = this
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(context, "ERR: $error", Toast.LENGTH_LONG).show()
        }
    }

    private fun initPicasso(context: Context): Picasso {
        return Picasso.Builder(context)
            .downloader(OkHttp3Downloader(buildHttpClient(context)))
            .build()
    }

    private fun buildHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest = chain.request().newBuilder().build()
            val response = chain.proceed(newRequest)
            val isBase64 = response.header("Base64-Encoded", "false")!!.toBoolean()
            if (isBase64) {
                val mediaType = response.body()!!.contentType()
                val base64 = response.body()!!.bytes()
                val decoded = Base64.decode(base64, Base64.DEFAULT)
                val responseBody = ResponseBody.create(mediaType, decoded)
                response.newBuilder().body(responseBody).build()
            } else response
        }.cache(Cache(context.cacheDir, 2 * 1024 * 1024)).build()
    }

}