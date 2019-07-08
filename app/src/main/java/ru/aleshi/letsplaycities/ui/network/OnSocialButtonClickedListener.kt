package ru.aleshi.letsplaycities.ui.network

import android.app.Activity
import android.view.View
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager

class OnSocialButtonClickedListener(private val activity: Activity, private val serviceType: ServiceType) :
    View.OnClickListener {

    override fun onClick(view: View) {
        SocialNetworkManager.login(serviceType, activity)
    }

}