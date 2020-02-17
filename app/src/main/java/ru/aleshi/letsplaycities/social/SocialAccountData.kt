package ru.aleshi.letsplaycities.social

import android.net.Uri
import ru.quandastudio.lpsclient.model.AuthType

data class SocialAccountData(
    val snUID: String,
    val login: String,
    val accessToken: String,
    val pictureUri: Uri,
    val networkType: AuthType
)