package ru.aleshi.letsplaycities.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_multiplayer.*
import ru.aleshi.letsplaycities.R


class MultiplayerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_multiplayer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        create.setOnClickListener {

        }
    }

}