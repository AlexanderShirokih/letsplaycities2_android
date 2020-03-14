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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameState
import ru.aleshi.letsplaycities.base.game.GameViewModel
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.databinding.FragmentGameBinding
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.game.listadapter.GameAdapter
import ru.aleshi.letsplaycities.utils.Event
import ru.aleshi.letsplaycities.utils.SpeechRecognitionHelper
import ru.aleshi.letsplaycities.utils.StringUtils.toTitleCase
import ru.quandastudio.lpsclient.core.LPSMessage
import javax.inject.Inject

class GameFragment : Fragment() {

    private lateinit var gameViewModel: GameViewModel
    private lateinit var correctionViewModel: CorrectionViewModel
    private lateinit var adapter: GameAdapter
    private lateinit var adManager: AdManager

    @Inject
    lateinit var prefs: GamePreferences
    @Inject
    lateinit var viewModelFactory: GameViewModelFactory
    @Inject
    lateinit var gameStateNotifier: GameStateNotifier

    private var clickSound: MediaPlayer? = null
    private val screenReceiver = ScreenReceiver { gameViewModel.onPlayerSurrender() }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        val activity = requireActivity()

        adapter = GameAdapter(activity) { recyclerView.postDelayed(::scrollRecyclerView, 200) }
        activity.onBackPressedDispatcher.addCallback(this) {
            showGoToMenuDialog()
        }
        if (prefs.isSoundEnabled()) {
            clickSound = MediaPlayer.create(activity, R.raw.click)
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
        val viewModelProvider =
            ViewModelProvider(requireParentFragment())
        correctionViewModel = viewModelProvider[CorrectionViewModel::class.java]
        viewModelProvider[ConfirmViewModel::class.java].callback.observe(
            viewLifecycleOwner,
            Observer {
                when {
                    it.checkWithResultCode(GO_TO_MENU) -> findNavController().popBackStack(
                        R.id.mainMenuFragment,
                        false
                    )
                    it.checkWithResultCode(SURRENDER) -> gameViewModel.onPlayerSurrender()
                    it.checkWithResultCode(USE_HINT) -> adManager.showAd()
                }
            })
        viewModelProvider[GameSessionViewModel::class.java].apply {
            gameViewModel.startGame(gameSession.value!!.peekContent())
        }
        viewModelProvider[UserMenuViewModel::class.java].actions.observe(
            viewLifecycleOwner,
            ::handleUserMenuAction
        )

        gameViewModel.apply {
            cities.observe(this@GameFragment, adapter::updateEntities)
            state.observe(this@GameFragment, ::handleState)
            state.observe(this@GameFragment, gameStateNotifier::showState)
            wordState.observe(this@GameFragment, ::handleWordResult)
            friendRequest.observe(this@GameFragment, ::handleFriendRequest)
            correctionViewModel.setCorrectionsList(wordState, ::processCityInput)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentGameBinding.inflate(inflater, container, false).apply {
            viewModel = gameViewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        (activity as MainActivity).setToolbarVisibility(false)

        checkForFirstLaunch()
        setupCityListeners(activity)
        setupMessageListeners()

        adManager = AdManager(adView, activity) { gameViewModel.useHintForPlayer() }
        adManager.setupAds()

        btnMenu.setOnClickListener { showGoToMenuDialog() }
        btnSurrender.setOnClickListener { showConfirmationDialog(SURRENDER, R.string.surrender) }
        btnHelp.setOnClickListener { showConfirmationDialog(USE_HINT, R.string.use_hint) }
        btnMsg.setOnClickListener { setMessagingLayout(messageInputLayout.visibility != View.VISIBLE) }
        avatarLeft.setOnClickListener { gameViewModel.showMenu(Position.LEFT, ::showUserMenu) }
        avatarRight.setOnClickListener { gameViewModel.showMenu(Position.RIGHT, ::showUserMenu) }

        recyclerView.apply {
            adapter = this@GameFragment.adapter
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

    /**
     * TODO: Create new hints
     */
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
        }
    }

    private fun submitMessage() {
        val message = messageInput.text!!.toString()
        if (message.isNotBlank()) {
            gameViewModel.processMessage(message) {
                clickSound?.start()
                scrollRecyclerView()
            }
            messageInput.text = null
            setMessagingLayout(false)
        }
    }

    private fun handleWordResult(wordResult: WordCheckingResult) {
        when (wordResult) {
            is WordCheckingResult.WrongLetter -> showInfo(
                getString(
                    R.string.wrong_letter,
                    wordResult.validLetter.toUpperCase()
                )
            )
            is WordCheckingResult.AlreadyUsed -> showInfo(
                getString(
                    R.string.already_used,
                    toTitleCase(wordResult.word)
                )
            )
            is WordCheckingResult.Exclusion -> showInfo(wordResult.description)
            is WordCheckingResult.Corrections ->
                navigateOnDestinationWaiting(GameFragmentDirections.showCorrectionTipsDialog())
            is WordCheckingResult.NotFound -> showInfo(
                getString(
                    R.string.city_not_found,
                    toTitleCase(wordResult.word)
                )
            )
            is WordCheckingResult.Accepted -> {
                cityInput.text = null
                clickSound?.start()
                hideKeyboard()
            }
        }
    }

    private fun handleState(currentState: GameState) {
        if (currentState is GameState.Finish) navigateOnDestinationWaiting(
            GameFragmentDirections.showGameResultDialog(
                currentState.gameResultMessage,
                currentState.playerScore
            )
        )
    }

    private fun handleUserMenuAction(event: Event<UserMenuViewModel.UserMenuAction>) {
        event.getContentIfNotHandled()?.apply {
            when (action) {
                UserMenuViewModel.Action.BanUser -> gameViewModel.banUser(userId) {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.user_banned, login),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack(R.id.gameFragment, true)
                }
                UserMenuViewModel.Action.SendFriendRequest -> gameViewModel.sendFriendRequest(userId)
                {
                    Toast.makeText(
                        requireActivity(),
                        R.string.new_friend_request,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleFriendRequest(request: LPSMessage.LPSFriendRequest) {
        when (request.result) {
            LPSMessage.FriendRequest.ACCEPTED -> showInfo(
                getString(R.string.friends_request_accepted, request.login)
            )
            LPSMessage.FriendRequest.DENIED -> showInfo(getString(R.string.friends_request_denied))
            else -> Unit
        }
    }

    private fun scrollRecyclerView() {
        recyclerView?.scrollToPosition(adapter.itemCount - 1)
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
        safeNavigate(
            GameFragmentDirections.showConfimationDialog(
                code,
                getString(msg),
                null
            )
        )
    }

    override fun onStop() {
        super.onStop()
        gameStateNotifier.hide()
        hideKeyboard()
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().unregisterReceiver(screenReceiver)
    }

    private fun showInfo(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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

    /**
     * Waits until currentDestination become gameFragment and then navigates to [dir].
     */
    private fun navigateOnDestinationWaiting(dir: NavDirections) {
        lifecycleScope.launch {
            val nav = findNavController()
            while (R.id.gameFragment != nav.currentDestination?.id)
                delay(100)
            nav.navigate(dir)
        }
    }

    /**
     * Navigates to [dir] only if current destination is gameFragment.
     */
    private fun safeNavigate(dir: NavDirections) {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.gameFragment)
            navController.navigate(dir)
    }

    private fun showUserMenu(user: User) {
        safeNavigate(
            GameFragmentDirections.showUserContextDialog(
                user.playerData.isFriend,
                user.name,
                user.credentials.userId
            )
        )
    }

    companion object {
        private const val GO_TO_MENU = 21
        private const val SURRENDER = 22
        private const val USE_HINT = 23
    }
}