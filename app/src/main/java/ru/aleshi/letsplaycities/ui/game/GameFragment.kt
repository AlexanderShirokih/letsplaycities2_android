package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.graphics.drawable.Drawable
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
import kotlinx.android.synthetic.main.fragment_game.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.GameContract
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.databinding.FragmentGameBinding
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.MainActivity
import ru.aleshi.letsplaycities.ui.confirmdialog.ConfirmViewModel

class GameFragment : Fragment(), GameContract.View {

    private lateinit var mBinding: FragmentGameBinding
    private lateinit var mGameViewModel: GameViewModel
    private lateinit var mGameSessionViewModel: GameSessionViewModel
    private lateinit var mGameSession: GameSession
    private lateinit var mAdapter: GameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity() as MainActivity
        ViewModelProviders.of(activity)[ConfirmViewModel::class.java].callback.observe(this, Observer {
            if (it.checkResultCode(GO_TO_MENU) && it.result) {
                findNavController().popBackStack(R.id.mainMenuFragment, false)
            }
        })

        mGameViewModel = ViewModelProviders.of(this)[GameViewModel::class.java]
        mGameSessionViewModel = ViewModelProviders.of(activity)[GameSessionViewModel::class.java].apply {
            gameSession.observe(this@GameFragment, Observer {
                mGameSession = it.apply { onAttachView(this@GameFragment) }
            })
        }
        mAdapter = GameAdapter(activity)
        activity.onBackPressedDispatcher.addCallback(this) {
            showGoToMenuDialog()
        }
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

        textInputLayout.setEndIconOnClickListener { submit() }
        // Converting to lambda gives an error: event is a nullable type
        cityInput.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    submit()
                return true
            }
        })

        btnMenu.setOnClickListener {
            showGoToMenuDialog()
        }

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
        findNavController().navigate(
            GameFragmentDirections.showConfirmationDialog(
                GO_TO_MENU,
                getString(R.string.go_to_menu),
                null
            )
        )
    }

    override fun onStop() {
        super.onStop()
        mGameSession.dispose()
    }


    override fun showToast(msg: String) {
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
        //TODO: clickPlayer?.start();
        mAdapter.addCity(city, countryCode, left)
        hideKeyboard()
        scrollRecyclerView()
    }

    override fun updateCity(city: String, hasErrors: Boolean) {
        mAdapter.updateCity(city, hasErrors)
    }

    override fun context(): Context = requireContext()

    companion object {
        private const val GO_TO_MENU = 1
    }
}