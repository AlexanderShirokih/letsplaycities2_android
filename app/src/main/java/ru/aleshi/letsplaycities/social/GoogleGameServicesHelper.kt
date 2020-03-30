package ru.aleshi.letsplaycities.social

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
import javax.inject.Inject

/**
 * This class contains functions for work with Google SignIn and Google Game Services
 */
class GoogleGameServicesHelper @Inject constructor(
    private val activity: BaseAsyncActivity
) : AchievementService {

    /**
     * Unlocks or increments achievement on play games server only if user logged in
     * @param achievement the achievement to be unlocked
     */
    override fun unlockAchievement(achievement: Achievement, incrementCount: Int) {
        GoogleSignIn.getLastSignedInAccount(activity)?.apply {
            val client = Games.getAchievementsClient(activity, this)
            if (achievement.isIncremental) {
                client.increment(
                    activity.getString(achievement.res),
                    incrementCount / achievement.scaleFactor
                )
            } else {
                client.unlock(activity.getString(achievement.res))
            }
        }
    }

    /**
     * Submits score to play games server only if user logged in
     * @param score user score to be submitted
     */
    override fun submitScore(score: Int) {
        GoogleSignIn.getLastSignedInAccount(activity)?.apply {
            Games.getLeaderboardsClient(activity, this)
                .submitScore(activity.getString(R.string.score_leaderboard), score.toLong())
        }
    }

    /**
     * Launches signOut task and awaits for it completion
     */
    suspend fun signOut() {
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
    suspend fun signIn(): Result<GoogleSignInAccount> {
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
    suspend fun showAchievementsIntent() {
        getAchievementsClient(true)
            ?.achievementsIntent?.await()
            ?.apply { activity.launchIntentAsync(this).await() }
    }

    /**
     * Starts sign in sequence if needed and shows leaderboard activity
     */
    suspend fun showLeaderboardIntent() {
        getLeaderboardsClient(true)
            ?.getLeaderboardIntent(activity.getString(R.string.score_leaderboard))
            ?.await()
            ?.apply { activity.launchIntentAsync(this).await() }
    }

    private suspend fun getAchievementsClient(
        autoSignIn: Boolean
    ): AchievementsClient? {
        val account = if (autoSignIn) signIn().valueOrNull
        else GoogleSignIn.getLastSignedInAccount(activity)
        return account?.run { Games.getAchievementsClient(activity, this) }
    }

    private suspend fun getLeaderboardsClient(
        autoSignIn: Boolean
    ): LeaderboardsClient? {
        val account = if (autoSignIn) signIn().valueOrNull
        else GoogleSignIn.getLastSignedInAccount(activity)

        return if (account != null) {
            Games.getLeaderboardsClient(activity, account)
        } else null
    }
}