package ru.aleshi.letsplaycities.ui.network

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_login.*
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.social.NativeAccess
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager


class LoginDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.login_no_sn)
            .setView(R.layout.dialog_login)
            .setCancelable(false)
            .setPositiveButton(R.string.enter) { _, _ ->
                val input = requireDialog().messageInputField.text.toString()
                if (input.length in 4..20) {
                    (ServiceType.NV.network as NativeAccess).userLogin = input
                    SocialNetworkManager.login(ServiceType.NV, requireActivity())
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
    }
}