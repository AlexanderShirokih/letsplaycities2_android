package ru.aleshi.letsplaycities.ui.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardedVideoAd
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_game.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.dictionary.DictionaryUpdater
import ru.aleshi.letsplaycities.base.game.GameContract
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.databinding.FragmentGameBinding
import ru.aleshi.letsplaycities.network.NetworkUtils.handleError
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
    private var mGameSession: GameContract.Presenter? = null
    private lateinit var mAdapter: GameAdapter
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    private var mClickSound: MediaPlayer? = null
    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity() as MainActivity
        ViewModelProviders.of(activity)[ConfirmViewModel::class.java].callback.observe(
            this,
            Observer {
                when {
                    it.checkWithResultCode(GO_TO_MENU) -> findNavController().popBackStack(
                        R.id.mainMenuFragment,
                        false
                    )
                    it.checkWithResultCode(SURRENDER) -> mGameSession?.onSurrender()
                    it.checkWithResultCode(USE_HINT) -> showAd()
                    it.checkAnyWithResultCode(NEW_FRIEND_REQUEST) -> mGameSession?.onFriendRequestResult(
                        it.result
                    )?.subscribe({}, { err -> handleError(err, this) })
                }
            })

        mGameViewModel = ViewModelProviders.of(this)[GameViewModel::class.java]
        mGameSessionViewModel =
            ViewModelProviders.of(activity)[GameSessionViewModel::class.java].apply {
                mGameSession = gameSession

                correctedWord.observe(this@GameFragment, Observer {
                    if (it != null) {
                        mGameSession?.postCorrectedWord(it.first, it.second)
                        it.first?.let { cityInput.text = null }
                        correctedWord.value = null
                    }
                })
                restart.observe(this@GameFragment, Observer {
                    if (it) {
                        stopGame()
                        startGame()
                        restart.value = false
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

    override fun comboSystemView(): ComboSystemView {
        val view = layoutInflater.inflate(R.layout.combo_badge, badgeRoot, false)
        badgeRoot.addView(view)
        return ComboBadgeView(view)
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
            rewardedVideoAdListener = TipsListener(::loadRewardedVideoAd, mGameSession!!::useHint)
        }
        if (!mRewardedVideoAd.isLoaded)
            loadRewardedVideoAd()
    }

    private fun showAd() {
        if (mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        } else {
            loadRewardedVideoAd()
            Toast.makeText(requireContext(), R.string.internet_unavailable, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(
            requireContext().getString(R.string.rewarded_ad_id),
            AdRequest.Builder().build()
        )
    }

    override fun getGamePreferences(): GamePreferences = lpsApplication.gamePreferences

    override fun onHighlightUser(position: Position) {
        when (position) {
            Position.LEFT -> mGameViewModel.isLeftActive.set(true)
            Position.RIGHT -> mGameViewModel.isLeftActive.set(false)
            else -> Unit
        }
    }

    override fun onTimerUpdate(time: String) {
        gameTimer.text = time
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<FragmentGameBinding>(
            inflater,
            R.layout.fragment_game,
            container,
            false
        ).apply {
            viewModel = mGameViewModel
            mBinding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        (activity as MainActivity).setToolbarVisibility(false)

        if (mGameSession == null) {
            showInfo(getString(R.string.game_session_is_null_error))
            Crashlytics.log("Game session is null!")
            try {
                getActivity()?.supportFragmentManager?.popBackStack()
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }
            return
        }

        checkForFirstLaunch()
        setupCityListeners(activity)
        setupMessageListeners()
        setupAds(activity)

        btnMenu.setOnClickListener { showGoToMenuDialog() }
        btnSurrender.setOnClickListener { showConfirmationDialog(SURRENDER, R.string.surrender) }
        btnHelp.setOnClickListener { showConfirmationDialog(USE_HINT, R.string.use_hint) }
        btnMsg.setOnClickListener { setMessagingLayout(messageInputLayout.visibility != View.VISIBLE) }
        avatarLeft.setOnClickListener { mGameSession?.needsShowMenu(Position.LEFT) }
        avatarRight.setOnClickListener { mGameSession?.needsShowMenu(Position.RIGHT) }

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

    private fun checkForFirstLaunch() {
        if (getGamePreferences().isFirstLaunch()) {
            val context = requireContext()
            resources.getStringArray(R.array.hints).forEachIndexed { i, hint ->
                Handler().postDelayed({
                    Toast.makeText(context, hint, Toast.LENGTH_LONG).show()
                }, 3600L * i)
            }
        }
    }

    private fun setupCityListeners(activity: Activity) {
        textInputLayout.setStartIconOnClickListener {
            SpeechRecognitionHelper.speech(
                this,
                activity
            )
        }
        textInputLayout.setEndIconOnClickListener { submit() }
        cityInput.setOnEditorActionListener { _, actionId, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                submit()
            true
        }
    }

    private fun setupMessageListeners() {
        messageInputLayout.setStartIconOnClickListener { setMessagingLayout(false) }
        messageInputLayout.setEndIconOnClickListener { submitMessage() }
        messageInput.setOnEditorActionListener { _, actionId, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                submitMessage()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        SpeechRecognitionHelper.onActivityResult(requestCode, resultCode, data) {
            mGameSession?.submit(it) {} ?: false
        }
    }

    private fun submit() {
        mGameSession?.submit(cityInput.text.toString()) {
            cityInput.text = null
        }
    }

    private fun submitMessage() {
        val message = messageInput.text!!.toString()

        if (message.isNotBlank()) {
            mGameSession!!.sendMessage(message)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    messageInput.text = null
                    setMessagingLayout(false)
                }
                .subscribe({}, { err -> handleError(err, this) })
                .addTo(disposable)
        }
    }

    private fun scrollRecyclerView() {
        if (recyclerView != null)
            recyclerView.scrollToPosition(mAdapter.itemCount - 1)
        else
            Crashlytics.log("Can't scroll because recyclerView is null!")
    }

    override fun showFriendRequestDialog(name: String) {
        val msg = getString(R.string.confirm_friend_request, name)
        findNavController().navigate(
            GameFragmentDirections.showConfimationDialog(
                NEW_FRIEND_REQUEST,
                msg,
                null
            )
        )
    }

    private fun hideKeyboard() {
        val activity = requireActivity()
        val inputManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            .subscribe {
                nav.navigate(
                    GameFragmentDirections.showGameResultDialog(
                        result,
                        score
                    )
                )
            })
    }

    private fun startGame() {
        mGameSession?.onAttachView(this)
        mAdapter.clear()
    }

    private fun stopGame() {
        hideKeyboard()
        disposable.clear()
        mGameSession?.onStop()
    }

    override fun onStart() {
        super.onStart()
        startGame()
    }

    override fun onStop() {
        stopGame()
        super.onStop()
    }

    override fun onDetach() {
        super.onDetach()
        mGameSession?.onDetachView()
    }

    override fun showInfo(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showUserMenu(isFriend: Boolean, name: String, userId: Int) {
        findNavController().navigate(
            GameFragmentDirections.showUserContextDialog(
                isFriend,
                name,
                userId
            )
        )
    }

    override fun showError(err: Throwable) {
        handleError(err, this)
    }

    override fun updateLabel(info: String, position: Position) {
        when (position) {
            Position.LEFT -> mGameViewModel.infoLeft.set(info)
            Position.RIGHT -> mGameViewModel.infoRight.set(info)
            else -> Unit
        }
    }

    override fun updateAvatar(image: Drawable, position: Position) {
        when (position) {
            Position.LEFT -> mGameViewModel.avatarLeft.set(image)
            Position.RIGHT -> mGameViewModel.avatarRight.set(image)
            else -> Unit
        }
    }

    override fun putMessage(message: String, position: Position) {
        mClickSound?.start()
        mAdapter.addMessage(message, position)
        scrollRecyclerView()
    }

    override fun putCity(city: String, countryCode: Short, position: Position) {
        mClickSound?.start()
        mAdapter.addCity(city, countryCode, position)
        Completable.fromAction { hideKeyboard() }
            .andThen(Completable.timer(200, TimeUnit.MILLISECONDS))
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(Completable.fromAction { scrollRecyclerView() })
            .subscribe()
    }

    override fun updateCity(city: String, hasErrors: Boolean) {
        mAdapter.updateCity(city, hasErrors)
    }

    override fun showCorrectionDialog(word: String, errorMsg: String) {
        findNavController().apply {
            if (R.id.gameFragment == currentDestination?.id ?: 0)
                navigate(GameFragmentDirections.showCorrectionTipsDialog(word, errorMsg))
        }
    }

    private fun setMessagingLayout(isMessagingMode: Boolean) {
        TransitionManager.beginDelayedTransition(root)
        messageInputLayout.visibility = if (isMessagingMode) {
            messageInput.requestFocus()
            View.VISIBLE
        } else View.GONE
        textInputLayout.visibility = if (isMessagingMode) View.GONE else {
            cityInput.requestFocus()
            View.VISIBLE
        }
    }

    override fun context(): Context = requireContext()

    companion object {
        private const val GO_TO_MENU = 21
        private const val SURRENDER = 22
        private const val USE_HINT = 23
        private const val NEW_FRIEND_REQUEST = 24
    }
}