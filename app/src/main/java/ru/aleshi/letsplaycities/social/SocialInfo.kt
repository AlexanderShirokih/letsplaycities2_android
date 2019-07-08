package ru.aleshi.letsplaycities.social

import ru.aleshi.letsplaycities.base.GamePreferences


class SocialInfo(var login: String, var id: String, var snName: String) {

    private val hash: String = SocialUtils.md5("$login,$id,$snName,")

    fun saveToPreferences(prefs: GamePreferences, tkn: String) {
        val editor = prefs.edit()
        editor.putString("sn_login", login)
        editor.putString("sn_uid", id)
        editor.putString("sn_name", snName)
        editor.putString("hash", hash)
        editor.putString("acc_tkn", tkn)
        editor.apply()
    }
}
