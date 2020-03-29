package ru.aleshi.letsplaycities.social

import android.app.Activity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.LeaderboardsClient
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.Result
import ru.aleshi.letsplaycities.ui.BaseAsyncActivity
import ru.aleshi.letsplaycities.utils.GoogleServicesExt.await
import ru.quandastudio.lpsclient.LPSException

/**
 * This class contains functions for work with Google SignIn and Google Game Services
 */
object GoogleServicesHelper {

    /**
     * Unlocks or increments achievement on play games server only if user logged in
     * @param achievement the achievement to be unlocked
     * @return `true` if user signed in or `false` if not
     */
    suspend fun unlockAchievement(activity: BaseAsyncActivity, achievement: Achievement): Boolean {
        getAchievementsClient(activity, false)?.apply {
            if (achievement.isIncremental) {
                increment(activity.getString(achievement.res), 1)
            } else {
                unlock(activity.getString(achievement.res))
            }
            return true
        }
        return false
    }

    /**
     * Submits score to play games server only if user logged in
     * @param score user score to be submitted
     * @return `true` if user signed in or `false` if not
     */
    suspend fun submitScore(activity: BaseAsyncActivity, score: Int): Boolean {
        getLeaderboardsClient(activity, false)?.apply {
            submitScore(
                activity.getString(R.string.score_leaderboard),
                score.toLong()
            )
            return true
        }
        return false
    }

    /**
     * Launches signOut task and awaits for it completion
     */
    suspend fun signOut(activity: Activity) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(activity.getString(R.string.default_web_client_id))
            .requestId().requestProfile().build()
        val client = GoogleSignIn.getClient(activity, options)
        client.signOut().await()
    }

    /**
     * Starts sign in flow. If last signed account is available it will returned,
     * otherwise silent sign in flow will started, and if no way to login from silent mode
     * sign in activity will launched and then result from it will returned.
     * @return [Result.Success] if sign in successful, [Result.Failure] is can't login
     */
    suspend fun signIn(activity: BaseAsyncActivity): Result<GoogleSignInAccount> {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(activity.getString(R.string.default_web_client_id))
            .requestId().requestProfile().build()
        val account: GoogleSignInAccount =
            GoogleSignIn.getAccountForExtension(activity, Games.GamesOptions.builder().build())

        if (GoogleSignIn.hasPermissions(account, *options.scopeArray)) {
            return Result.success(account)
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            val signInClient = GoogleSignIn.getClient(activity, options)
            try {
                return Result.success(signInClient.silentSignIn().await())
            } catch (e: ApiException) {
                e.printStackTrace()

                // We can't silently sign in, so try to sign in implicitly
                val data = activity.launchIntentAsync(signInClient.signInIntent).await()

                return if (data != null) {
                    val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data.data)
                    if (result.isSuccess) {
                        Result.success(result.signInAccount!!)
                    } else {
                        val error = result.status.statusMessage
                            ?: "Authentication error: ${CommonStatusCodes.getStatusCodeString(
                                result.status.statusCode
                            )}"
                        Result.failure(LPSException(error))
                    }
                } else Result.failure(LPSException(activity.getString(R.string.no_game_services)))
            }
        }
    }

    /**
     * Starts sign in sequence if needed and shows achievements activity
     */
    suspend fun showAchievementsIntent(activity: BaseAsyncActivity) {
        getAchievementsClient(activity, true)
            ?.achievementsIntent?.await()
            ?.apply { activity.launchIntentAsync(this).await() }
    }

    /**
     * Starts sign in sequence if needed and shows leaderboard activity
     */
    suspend fun showLeaderboardIntent(context: BaseAsyncActivity) {
        getLeaderboardsClient(context, true)
            ?.getLeaderboardIntent(context.getString(R.string.score_leaderboard))
            ?.await()
            ?.apply { context.launchIntentAsync(this).await() }
    }

    private suspend fun getAchievementsClient(
        activity: BaseAsyncActivity,
        autoSignIn: Boolean
    ): AchievementsClient? {
        val account = if (autoSignIn) signIn(activity).valueOrNull
        else GoogleSignIn.getLastSignedInAccount(activity)
        return account?.run { Games.getAchievementsClient(activity, this) }
    }

    private suspend fun getLeaderboardsClient(
        activity: BaseAsyncActivity,
        autoSignIn: Boolean
    ): LeaderboardsClient? {
        val account = if (autoSignIn) signIn(activity).valueOrNull
        else GoogleSignIn.getLastSignedInAccount(activity)

        return if (account != null) {
            Games.getLeaderboardsClient(activity, account)
        } else null
    }
}