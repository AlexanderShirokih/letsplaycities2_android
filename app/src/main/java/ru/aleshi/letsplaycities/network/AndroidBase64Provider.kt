package ru.aleshi.letsplaycities.network

import android.util.Base64
import ru.quandastudio.lpsclient.core.Base64Provider

object AndroidBase64Provider : Base64Provider {

    override fun decode(data: String): ByteArray =
        Base64.decode(data, Base64.NO_WRAP)

    override fun encode(data: ByteArray): String =
        Base64.encodeToString(data, Base64.NO_WRAP)
}