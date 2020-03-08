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
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_game.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameViewModel
import ru.aleshi.letsplaycities.base.game.WordCheckingResult
import ru.aleshi.letsplaycities.databinding.FragmentGameBinding
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel
import ru.aleshi.letsplaycities.ui.game.listadapter.GameAdapter
import ru.aleshi.letsplaycities.utils.SpeechRecognitionHelper
import ru.aleshi.letsplaycities.utils.StringUtils.toTitleCase
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

        adapter = GameAdapter(activity)
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
//                    it.checkAnyWithResultCode(NEW_FRIEND_REQUEST) -> mGameSession?.onFriendRequestResult(it.result
//                    )?.subscribe({}, { err -> showErrorSnackbar(err, this) })
                }
            })
        viewModelProvider[GameSessionViewModel::class.java].apply {
            gameViewModel.startGame(gameSession.value!!.peekContent())
        }

        gameViewModel.apply {
            cities.observe(this@GameFragment, adapter::updateEntities)
            state.observe(this@GameFragment, gameStateNotifier::showState)
            wordState.observe(this@GameFragment, ::handleWordResult)
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

        adManager = AdManager(adView, activity) {
            gameViewModel.useHintForPlayer()
        }
        adManager.setupAds()

        btnMenu.setOnClickListener { showGoToMenuDialog() }
        btnSurrender.setOnClickListener { showConfirmationDialog(SURRENDER, R.string.surrender) }
        btnHelp.setOnClickListener { showConfirmationDialog(USE_HINT, R.string.use_hint) }
        btnMsg.setOnClickListener { setMessagingLayout(messageInputLayout.visibility != View.VISIBLE) }
//        avatarLeft.setOnClickListener { mGameSession?.needsShowMenu(Position.LEFT) }
//        avatarRight.setOnClickListener { mGameSession?.needsShowMenu(Position.RIGHT) }

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
            is WordCheckingResult.AlreadyUsed -> showInfo(
                getString(
                    R.string.already_used,
                    toTitleCase(wordResult.word)
                )
            )
            is WordCheckingResult.Exclusion -> showInfo(wordResult.description)
            is WordCheckingResult.Corrections ->
                findNavController().navigate(R.id.showCorrectionTipsDialog)
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
                recyclerView.postDelayed({
                    scrollRecyclerView()
                }, 200)
            }
        }
    }

    private fun scrollRecyclerView() {
        recyclerView?.scrollToPosition(adapter.itemCount - 1)
    }

    fun showFriendRequestDialog(name: String) {
        findNavController().navigate(
            GameFragmentDirections.showConfimationDialog(
                NEW_FRIEND_REQUEST,
                getString(R.string.confirm_friend_request, name),
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
        findNavController().navigate(
            GameFragmentDirections.showGameResultDialog(
                result,
                score
            )
        )
    }

    private fun stopGame() {
        hideKeyboard()
        gameViewModel.dispose()
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

    fun showUserMenu(isFriend: Boolean, name: String, userId: Int) {
        findNavController().navigate(
            GameFragmentDirections.showUserContextDialog(
                isFriend,
                name,
                userId
            )
        )
    }

    companion object {
        private const val GO_TO_MENU = 21
        private const val SURRENDER = 22
        private const val USE_HINT = 23
        private const val NEW_FRIEND_REQUEST = 24
    }
}