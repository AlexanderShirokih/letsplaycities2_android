package ru.aleshi.letsplaycities.ui.network

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.base.player.GamePlayerDataFactory
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.ViewModelFactory
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import ru.quandastudio.lpsclient.NetworkRepository
import javax.inject.Inject

abstract class BasicNetworkFetchFragment<FetchDataType> : Fragment() {

    @Inject
    protected lateinit var viewModelFactory: ViewModelFactory
    @Inject
    protected lateinit var mNetworkRepository: NetworkRepository
    @Inject
    protected lateinit var mGamePlayerDataFactory: GamePlayerDataFactory

    private lateinit var mApplication: LPSApplication

    private val mDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mApplication = lpsApplication
        onCreate(
            ViewModelProviders.of(
                requireActivity(),
                viewModelFactory
            )
        )
    }

    abstract fun onCreate(viewModelProvider: ViewModelProvider)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mGamePlayerDataFactory.load(mApplication.gamePreferences)?.let { userData ->
            mDisposable.addAll(
                mNetworkRepository.login(userData)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ fetchData() }, ::handleError)
            )
        }
    }

    private fun fetchData() {
        mDisposable.addAll(
            onStartRequest(mNetworkRepository)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onDataFetched, ::handleError)
        )
    }

    override fun onStop() {
        super.onStop()
        mDisposable.dispose()
        mNetworkRepository.disconnect()
    }

    abstract fun onStartRequest(networkRepository: NetworkRepository): Single<FetchDataType>

    abstract fun onDataFetched(result: FetchDataType)

    private fun handleError(t: Throwable) {
        NetworkUtils.handleError(t, this)
    }

}