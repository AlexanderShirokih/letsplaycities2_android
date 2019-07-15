package ru.aleshi.letsplaycities.ui.game

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_game.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.game.GameContract
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.databinding.FragmentGameBinding
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.ui.MainActivity

class GameFragment : Fragment(), GameContract.View {

    private lateinit var mBinding: FragmentGameBinding
    private lateinit var mGameViewModel: GameViewModel
    private lateinit var mGameSessionViewModel: GameSessionViewModel
    private lateinit var mGameSession: GameSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGameViewModel = ViewModelProviders.of(this)[GameViewModel::class.java]
        mGameSessionViewModel = ViewModelProviders.of(requireActivity())[GameSessionViewModel::class.java].apply {
            gameSession.observe(this@GameFragment, Observer {
                mGameSession = it.apply { onAttachView(this@GameFragment) }
            })
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
    }

    private fun submit() {
        mGameSession.submit(cityInput.text.toString()) {
            cityInput.text = null
        }
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

    override fun putCity(city: String, left: Boolean) {
        //TODO:
        Log.d("TAG", "Add city: $city")
    }

    override fun updateCity(city: String, hasErrors: Boolean) {
        //TODO:
        Log.d("TAG", "Put city: $city")
    }


    override fun context(): Context = requireContext()
}