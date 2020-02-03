package ru.aleshi.letsplaycities.base

import androidx.annotation.StringRes
import ru.aleshi.letsplaycities.R

enum class Achievement constructor(@StringRes val res: Int, val isIncremental: Boolean = false) {
    Write5Cities(R.string.ach_write_5c),

    Use3Tips(R.string.ach_tips_3, true),

    Add1Friend(R.string.ach_friend_1),

    PlayInHardMode(R.string.ach_hard_mode),

    Write40Cities(R.string.ach_write_40c),

    LoginViaSocial(R.string.ach_social),

    ChangeTheme(R.string.ach_theme),

    Write30CitiesInGame(R.string.ach_30c_in_game),

    PlayOnline3Times(R.string.ach_online_3_times, true),

    Use30Tips(R.string.ach_tips_30, true),

    Write500Cities(R.string.ach_write_500c),

    Write80CitiesInGame(R.string.ach_80c_in_game);
}