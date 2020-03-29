package ru.aleshi.letsplaycities.social

import android.app.Activity
import android.content.Context
import android.util.Log
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

object GoogleAccountHelper {


    private suspend fun getAchievementsClient(activity: BaseAsyncActivity): AchievementsClient? {
        val account = signIn(activity)
        Log.d("TAG", "Last SIGNED IN Account=$account")

        return if (account is Result.Success) {
            Log.d("TAG", "Retrieving acheivements client!")
            Games.getAchievementsClient(activity, account.value)
        } else null
    }

    private fun getLeaderboardsClient(context: Context): LeaderboardsClient? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null) {
            Games.getLeaderboardsClient(context, account)
        } else null
    }

    suspend fun unlockAchievement(context: BaseAsyncActivity, achievement: Achievement) {
        getAchievementsClient(context)?.apply {
            if (achievement.isIncremental) {
                increment(context.getString(achievement.res), 1)
            } else {
                unlock(context.getString(achievement.res))
            }
        }
    }

    fun submitScore(context: Context, score: Int) {
        getLeaderboardsClient(context)?.submitScore(
            context.getString(R.string.score_leaderboard),
            score.toLong()
        )
    }

    suspend fun signOut(activity: Activity) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(activity.getString(R.string.default_web_client_id))
            .requestId().requestProfile().build()
        val client = GoogleSignIn.getClient(activity, options)
        client.signOut().await()
    }

    suspend fun signIn(activity: BaseAsyncActivity): Result<GoogleSignInAccount> {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(activity.getString(R.string.default_web_client_id))
            .requestId().requestProfile().build()
        val account: GoogleSignInAccount =
            GoogleSignIn.getAccountForExtension(activity, Games.GamesOptions.builder().build())
        Log.d("TAG", "account=$account")

        if (GoogleSignIn.hasPermissions(account, *options.scopeArray)) {
            Log.d("TAG", "Short login!")
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

    suspend fun showAchievementsIntent(context: BaseAsyncActivity) {
        val client = getAchievementsClient(context)

        Log.d("TAG", "He we go")
        val result = client?.achievementsIntent?.await()

        Log.d("TAG", "Awaiting result!")
        result?.apply {
            Log.d("TAG", "Starting activity!")
            context.launchIntentAsync(this).await()
        }
    }

    suspend fun showLeaderboardIntent(context: BaseAsyncActivity) {
        getLeaderboardsClient(context)
            ?.getLeaderboardIntent(context.getString(R.string.score_leaderboard))
            ?.await()
            ?.apply { context.launchIntentAsync(this).await() }
    }
}