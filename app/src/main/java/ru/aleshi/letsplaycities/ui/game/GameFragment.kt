package ru.aleshi.letsplaycities.ui.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardedVideoAd
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_game.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameEntity
import ru.aleshi.letsplaycities.base.game.GameViewModel
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.databinding.FragmentGameBinding
import ru.aleshi.letsplaycities.network.NetworkUtils.showErrorSnackbar
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.utils.SpeechRecognitionHelper
import ru.aleshi.letsplaycities.utils.TipsListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GameFragment : Fragment() {

    private lateinit var viewBinding: FragmentGameBinding
    private lateinit var gameViewModel: GameViewModel
    private lateinit var gameSessionViewModel: GameSessionViewModel
    private lateinit var mAdapter: GameAdapter
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    @Inject
    lateinit var prefs: GamePreferences

    @Inject
    lateinit var viewModelFactory: GameViewModelFactory

    private var mClickSound: MediaPlayer? = null
    private val disposable: CompositeDisposable = CompositeDisposable()
    private val screenReceiver = ScreenReceiver {
        //        mGameSession?.onSurrender()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        mAdapter = GameAdapter(activity)
        activity.onBackPressedDispatcher.addCallback(this) {
            showGoToMenuDialog()
        }
        if (prefs.isSoundEnabled()) {
            mClickSound = MediaPlayer.create(activity, R.raw.click)
            screenReceiver.sound = MediaPlayer.create(activity, R.raw.notification)
        }

        // Register receiver for handling screen power off events
        activity.registerReceiver(screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            })
        gameViewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ViewModelProvider(this)[ConfirmViewModel::class.java].callback.observe(
            viewLifecycleOwner,
            Observer {
                when {
                    it.checkWithResultCode(GO_TO_MENU) -> findNavController().popBackStack(
                        R.id.mainMenuFragment,
                        false
                    )
//                    it.checkWithResultCode(SURRENDER) -> mGameSession?.onSurrender()
                    it.checkWithResultCode(USE_HINT) -> showAd()
//                    it.checkAnyWithResultCode(NEW_FRIEND_REQUEST) -> mGameSession?.onFriendRequestResult(it.result
//                    )?.subscribe({}, { err -> showErrorSnackbar(err, this) })
                }
            })

        gameSessionViewModel =
            ViewModelProvider(requireParentFragment())[GameSessionViewModel::class.java].apply {
                gameViewModel.startGame(gameSession.value!!.peekContent())
            }
    }

    private fun setupAds(activity: Activity) {
        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = AdListenerHelper(adView)
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity).apply {
            rewardedVideoAdListener = TipsListener(::loadRewardedVideoAd) {
                /*mGameSession::useHint*/
            }
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
            viewModel = gameViewModel
            viewBinding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        (activity as MainActivity).setToolbarVisibility(false)

        checkForFirstLaunch()
        setupCityListeners(activity)
        setupMessageListeners()
        setupAds(activity)

        btnMenu.setOnClickListener { showGoToMenuDialog() }
        btnSurrender.setOnClickListener { showConfirmationDialog(SURRENDER, R.string.surrender) }
        btnHelp.setOnClickListener { showConfirmationDialog(USE_HINT, R.string.use_hint) }
        btnMsg.setOnClickListener { setMessagingLayout(messageInputLayout.visibility != View.VISIBLE) }
//        avatarLeft.setOnClickListener { mGameSession?.needsShowMenu(Position.LEFT) }
//        avatarRight.setOnClickListener { mGameSession?.needsShowMenu(Position.RIGHT) }

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
        if (prefs.isFirstLaunch()) {
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
        SpeechRecognitionHelper.onActivityResult(
            requestCode,
            resultCode,
            data,
            gameViewModel::processCityInput
        )
    }

    private fun submit() {
        cityInput.apply {
            if (!text.isNullOrEmpty())
                gameViewModel.processCityInput(text.toString())
            //TODO: cityInput.text = null
        }
    }

    private fun submitMessage() {
        val message = messageInput.text!!.toString()
        if (message.isNotBlank()) {
            gameViewModel.processMessage(message)
            messageInput.text = null
            setMessagingLayout(false)
        }
    }

    private fun scrollRecyclerView() {
        if (recyclerView != null)
            recyclerView.scrollToPosition(mAdapter.itemCount - 1)
        else
            Crashlytics.log("Can't scroll because recyclerView is null!")
    }

    fun showFriendRequestDialog(name: String) {
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

    fun showGameResults(result: String, score: Int) {
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

    private fun stopGame() {
        hideKeyboard()
        disposable.clear()
//        mGameSession?.onStop()
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().unregisterReceiver(screenReceiver)
    }

    fun showInfo(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showUserMenu(isFriend: Boolean, name: String, userId: Int) {
        findNavController().navigate(
            GameFragmentDirections.showUserContextDialog(
                isFriend,
                name,
                userId
            )
        )
    }

    fun showError(err: Throwable) {
        showErrorSnackbar(err, this)
    }

    fun putMessage(message: String, position: Position) {
        mClickSound?.start()
        mAdapter.addMessage(message, position)
        scrollRecyclerView()
    }

    fun putCity(city: String, countryCode: Short, position: Position) {
        mClickSound?.start()
        mAdapter.addCity(city, countryCode, position)
        Completable.fromAction { hideKeyboard() }
            .andThen(Completable.timer(200, TimeUnit.MILLISECONDS))
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(Completable.fromAction { scrollRecyclerView() })
            .subscribe()
    }

    fun updateCity(city: String, hasErrors: Boolean) {
        mAdapter.updateCity(
            GameEntity.CityInfo(
                city = city,
                status = if (hasErrors) CityStatus.ERROR else CityStatus.OK,
                position = Position.UNKNOWN,
                countryCode = 0
            )
        )
    }

    fun showCorrectionDialog(word: String, errorMsg: String) {
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

    companion object {
        private const val GO_TO_MENU = 21
        private const val SURRENDER = 22
        private const val USE_HINT = 23
        private const val NEW_FRIEND_REQUEST = 24
    }
}