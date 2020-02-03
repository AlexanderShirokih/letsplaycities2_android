package ru.aleshi.letsplaycities.social

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.LeaderboardsClient
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.Achievement


class GoogleAccountHelper(val context: Activity) {
    companion object {
        private const val RC_ACHIEVEMENT_UI = 9003
        private const val RC_LEADERBOARD_UI = 9004

    }

    private val lastSignInAccount = GoogleSignIn.getLastSignedInAccount(context)

    private val achievementsClient: AchievementsClient? by lazy {
        if (lastSignInAccount != null) {
            Games.getAchievementsClient(context, lastSignInAccount)
        } else null
    }

    val leaderboardsClient: LeaderboardsClient? by lazy {
        if (lastSignInAccount != null) {
            Games.getLeaderboardsClient(context, lastSignInAccount)
        } else null
    }

    fun unlockAchievement(achievement: Achievement) {
        if (achievement.isIncremental) {
            achievementsClient?.increment(context.getString(achievement.res), 1)
        } else {
            achievementsClient?.unlock(context.getString(achievement.res))
        }
    }

    fun submitScore(score: Int) {
        leaderboardsClient?.submitScore(
            context.getString(R.string.score_leaderboard),
            score.toLong()
        )
    }

    fun showAchievementsIntent() {
        achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
            context.startActivityForResult(intent, RC_ACHIEVEMENT_UI)
        }
    }

    fun showLeaderboardIntent() {
        leaderboardsClient?.getLeaderboardIntent(context.getString(R.string.score_leaderboard))
            ?.addOnSuccessListener { intent ->
                context.startActivityForResult(intent, RC_LEADERBOARD_UI)
            }
    }
}