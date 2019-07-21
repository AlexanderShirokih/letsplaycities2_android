package ru.aleshi.letsplaycities.ui.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardedVideoAd
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_game.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameContract
import ru.aleshi.letsplaycities.databinding.FragmentGameBinding
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.utils.SpeechRecognitionHelper
import ru.aleshi.letsplaycities.utils.TipsListener
import ru.aleshi.letsplaycities.utils.Utils.lpsApplication
import java.util.concurrent.TimeUnit

class GameFragment : Fragment(), GameContract.View {

    private lateinit var mBinding: FragmentGameBinding
    private lateinit var mGameViewModel: GameViewModel
    private lateinit var mGameSessionViewModel: GameSessionViewModel
    private lateinit var mGameSession: GameContract.Presenter
    private lateinit var mAdapter: GameAdapter

    private lateinit var mRewardedVideoAd: RewardedVideoAd

    private var mClickSound: MediaPlayer? = null
    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity() as MainActivity
        ViewModelProviders.of(activity)[ConfirmViewModel::class.java].callback.observe(this, Observer {
            when {
                it.checkWithResultCode(GO_TO_MENU) -> findNavController().popBackStack(R.id.mainMenuFragment, false)
                it.checkWithResultCode(SURRENDER) -> mGameSession.onSurrender()
                it.checkWithResultCode(USE_HINT) -> showAd()
            }
        })

        mGameViewModel = ViewModelProviders.of(this)[GameViewModel::class.java]
        mGameSessionViewModel = ViewModelProviders.of(activity)[GameSessionViewModel::class.java].apply {
            gameSession.observe(this@GameFragment, Observer {
                mGameSession = it.apply { onAttachView(this@GameFragment) }
                setupAds(activity)
            })
            correctedWord.observe(this@GameFragment, Observer {
                if (it != null) {
                    mGameSession.postCorrectedWord(it.first, it.second)
                    it.first?.let { cityInput.text = null }
                    correctedWord.value = null
                }
            })
        }
        mAdapter = GameAdapter(activity)
        activity.onBackPressedDispatcher.addCallback(this) {
            showGoToMenuDialog()
        }
        if (getGamePreferences().isSoundEnabled()) {
            mClickSound = MediaPlayer.create(activity, R.raw.click)
        }
    }

    override fun downloadingListener(): DictionaryUpdater.DownloadingListener {
        return SnackbarDownloadingListener(this)
    }

    override fun setMenuItemsVisibility(help: Boolean, msg: Boolean) {
        mGameViewModel.helpBtnVisible.set(help)
        mGameViewModel.msgBtnVisible.set(msg)
    }

    private fun setupAds(activity: Activity) {
        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = object : AdListener() {
            override fun onAdClosed() {
                adView.visibility = View.GONE
            }

            override fun onAdLoaded() {
                adView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(error: Int) {
                adView.visibility = View.GONE
            }
        }
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity).apply {
            rewardedVideoAdListener = TipsListener(::loadRewardedVideoAd, mGameSession::useHint)
        }
        if (!mRewardedVideoAd.isLoaded)
            loadRewardedVideoAd()
    }

    private fun showAd() {
        if (mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        } else {
            loadRewardedVideoAd()
            Toast.makeText(requireContext(), R.string.internet_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(requireContext().getString(R.string.rewarded_ad_id), AdRequest.Builder().build())
    }

    override fun getGamePreferences(): GamePreferences = lpsApplication.gamePreferences

    override fun onHighlightUser(left: Boolean) {
        mGameViewModel.isLeftActive.set(left)
    }

    override fun onTimerUpdate(time: String) {
        gameTimer.text = time
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<FragmentGameBinding>(inflater, R.layout.fragment_game, container, false).apply {
            viewModel = mGameViewModel
            mBinding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        (activity as MainActivity).setToolbarVisibility(false)
        textInputLayout.setStartIconOnClickListener { SpeechRecognitionHelper.speech(this, activity) }
        textInputLayout.setEndIconOnClickListener { submit() }
        // Converting to lambda gives an error: event is a nullable type
        cityInput.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    submit()
                return true
            }
        })

        btnMenu.setOnClickListener { showGoToMenuDialog() }
        btnSurrender.setOnClickListener { showConfirmationDialog(SURRENDER, R.string.surrender) }
        btnHelp.setOnClickListener { showConfirmationDialog(USE_HINT, R.string.use_hint) }

        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(activity).apply { stackFromEnd = true }
            addOnLayoutChangeListener { _, _, _,
                                        _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    this.postDelayed({
                        scrollRecyclerView()
                    }, 100)
                }

            }
            setHasFixedSize(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        SpeechRecognitionHelper.onActivityResult(requestCode, resultCode, data) {
            mGameSession.submit(it) {}
        }
    }

    private fun submit() {
        mGameSession.submit(cityInput.text.toString()) {
            cityInput.text = null
        }
    }

    private fun scrollRecyclerView() {
        recyclerView.scrollToPosition(mAdapter.itemCount - 1)
    }

    private fun hideKeyboard() {
        val activity = requireActivity()
        val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        activity.currentFocus?.run {
            inputManager.hideSoftInputFromWindow(windowToken, HIDE_NOT_ALWAYS)
        }
    }

    private fun showGoToMenuDialog() {
        showConfirmationDialog(GO_TO_MENU, R.string.go_to_menu)
    }

    private fun showConfirmationDialog(code: Int, msg: Int) {
        findNavController().navigate(
            GameFragmentDirections.showConfimationDialog(
                code,
                getString(msg),
                null
            )
        )
    }

    override fun showGameResults(result: String, score: Int) {
        val nav = findNavController()
        disposable.add(Completable.timer(100, TimeUnit.MILLISECONDS)
            .repeatUntil { nav.currentDestination!!.id == R.id.gameFragment }
            .subscribe { nav.navigate(GameFragmentDirections.showGameResultDialog(result, score)) })
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

    override fun onDetach() {
        super.onDetach()
        mGameSession.onDetachView()
    }

    override fun showInfo(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showError(err: Throwable) {
        NetworkUtils.handleError(err, this)
    }

    override fun updateLabel(info: String, left: Boolean) {
        val field =
            if (left)
                mGameViewModel.infoLeft
            else
                mGameViewModel.infoRight
        field.set(info)
    }

    override fun updateAvatar(image: Drawable, left: Boolean) {
        val field =
            if (left)
                mGameViewModel.avatarLeft
            else
                mGameViewModel.avatarRight
        field.set(image)
    }

    override fun putCity(city: String, countryCode: Short, left: Boolean) {
        mClickSound?.start()
        mAdapter.addCity(city, countryCode, left)
        hideKeyboard()
        scrollRecyclerView()
    }

    override fun updateCity(city: String, hasErrors: Boolean) {
        mAdapter.updateCity(city, hasErrors)
    }


    override fun showCorrectionDialog(word: String, errorMsg: String) {
        findNavController().navigate(GameFragmentDirections.showCorrectionTipsDialog(word, errorMsg))
    }

    override fun context(): Context = requireContext()

    companion object {
        private const val GO_TO_MENU = 21
        private const val SURRENDER = 22
        private const val USE_HINT = 23
    }
}