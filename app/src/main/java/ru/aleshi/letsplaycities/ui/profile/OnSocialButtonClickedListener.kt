package ru.aleshi.letsplaycities.ui.profile

import android.app.Activity
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.aleshi.letsplaycities.social.ServiceType
import ru.aleshi.letsplaycities.social.SocialNetworkManager

class OnSocialButtonClickedListener(
    private val activity: Activity,
    private val serviceType: ServiceType,
    private val scope: CoroutineScope
) :
    View.OnClickListener {

    override fun onClick(view: View) {
        scope.launch {
            SocialNetworkManager.login(serviceType, activity)
        }
    }

}