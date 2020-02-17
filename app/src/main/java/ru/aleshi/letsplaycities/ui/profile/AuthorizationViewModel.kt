package ru.aleshi.letsplaycities.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.player.GameAuthDataFactory
import ru.aleshi.letsplaycities.social.SocialAccountData
import ru.aleshi.letsplaycities.social.SocialNetworkLoginListener
import ru.aleshi.letsplaycities.social.SocialNetworkManager
import ru.aleshi.letsplaycities.ui.FetchState
import ru.aleshi.letsplaycities.ui.network.FbToken
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.core.LpsApi
import ru.quandastudio.lpsclient.model.*
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class AuthorizationViewModel @Inject constructor(
    private val api: LpsApi,
    private val authDataFactory: GameAuthDataFactory,
    @FbToken private val firebaseToken: Single<String>
) : ViewModel() {

    private val mState = MutableLiveData<FetchState>()

    private val disposable = CompositeDisposable()

    val state: LiveData<FetchState>
        get() = mState

    /**
     * Call to register social network callback and start authorization sequence
     * if social network gives us user data
     */
    fun registerCallback() {
        SocialNetworkManager.registerCallback(object : SocialNetworkLoginListener {
            override fun onLoggedIn(data: SocialAccountData) {
                disposable.add(processAuthorizationSequence(data)
                    .doOnSubscribe { mState.postValue(FetchState.LoadingState) }
                    .doOnSuccess { mState.postValue(FetchState.DataState(R.string.comlpeted)) }
                    .subscribe({ resp ->
                        authDataFactory.save(
                            AuthData(
                                login = resp.name,
                                snType = resp.authType,
                                credentials = Credentials(
                                    userId = resp.userId,
                                    hash = resp.accHash
                                )
                            )
                        )
                        mState.postValue(FetchState.FinishState)
                    }, { err -> mState.postValue(FetchState.ErrorState(err)) })
                )
            }

            override fun onError() {
                mState.postValue(FetchState.ErrorState(Exception()))
            }
        })
    }

    /**
     * Begins authorization sequence which authorize user on server and uploads avatar to it
     * @param data authorization data with social network info
     * @return any error if something went wrong
     */
    private fun processAuthorizationSequence(data: SocialAccountData): Single<SignUpResponse> =
        processAvatar(data.pictureUri)
            .zipWith(signUp(data)) { pic: Pair<ByteArray, String>, resp: SignUpResponse ->
                Triple(pic.first, pic.second, resp)
            }
            .flatMap { t ->
                if (t.second.isNotEmpty() && t.second != t.third.picHash) {
                    uploadAvatar(t.first, t.second).map { t.third }
                } else
                    Single.just(t.third)
            }

    /**
     * Used to register user or logIn if it exists
     * @param data authorization data with social network info
     * @return SignUpResponse if authorization was successful or AuthorizationException if player was banned
     * or query data was incorrect, or another exception if something went wrong
     */
    private fun signUp(data: SocialAccountData): Single<SignUpResponse> =
        Single.just(FetchState.DataState(R.string.fetching_token))
            .doOnSuccess(mState::postValue)
            .flatMap { firebaseToken }
            .map { token ->
                SignUpRequest(
                    snUID = data.snUID,
                    login = data.login,
                    authType = data.networkType,
                    accToken = data.accessToken,
                    firebaseToken = token
                )
            }
            .doOnSuccess { mState.postValue(FetchState.DataState(R.string.authorization)) }
            .observeOn(Schedulers.io())
            .flatMap(api::signUp)
            .flatMap {
                if (it.error != null)
                    Single.error(AuthorizationException(it.error!!))
                else
                    Single.just(it.data!!)
            }

    /**
     * Uploads picture with its hash to API server as PNG format
     * @param picture image to be uploaded
     * @param hash MD5 hash of image
     * @return Response to "ok" or error
     */
    private fun uploadAvatar(picture: ByteArray, hash: String) =
        Single.just(FetchState.DataState(R.string.uploading_picture))
            .doOnSuccess(mState::postValue)
            .observeOn(Schedulers.io())
            .flatMap { api.updatePicture("png", hash, picture) }
            .flatMap(MessageWrapper<String>::toSingle)

    /**
     * Used to load image by given URI, then calculate MD5
     * @param uri image URI
     * @return Loaded image with its MD5 hash or pair of empty array and string if something went wrong
     */
    private fun processAvatar(uri: Uri): Single<Pair<ByteArray, String>> =
        Maybe.just(FetchState.DataState(R.string.exec_picture))
            .doOnSuccess(mState::postValue)
            .flatMap { loadAndResize(uri) }
            .flatMap(::encode)
            .observeOn(Schedulers.computation())
            .map(::calcMd5Hash)
            .switchIfEmpty(Single.just(ByteArray(0) to ""))

    /**
     * Loads and resized image by given uri
     * @param uri image URI
     * @return Loaded and resized to 128x128px image or empty if unable to load image
     */
    private fun loadAndResize(uri: Uri) = Maybe.fromCallable {
        Picasso.get()
            .load(uri)
            .networkPolicy(NetworkPolicy.NO_CACHE)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .resize(0, 128)
            .get()
    }
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())

    /**
     * Used to compress bitmap image to PNG format
     * @param bitmap input image
     * @return ByteArray of compressed image or empty result if something went wrong
     */
    private fun encode(bitmap: Bitmap): Maybe<ByteArray> {
        return ByteArrayOutputStream().run {
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, this))
                Maybe.just(toByteArray())
            else
                Maybe.empty()
        }
    }

    /**
     * Calculates MD5 hash code for input array
     * @param array input array
     * @return Pair of input array and computed hashCode
     */
    private fun calcMd5Hash(array: ByteArray): Pair<ByteArray, String> {
        val md = java.security.MessageDigest.getInstance("MD5")
        val digest = md.digest(array)
        val sb = StringBuilder()
        for (b in digest) {
            sb.append(Integer.toHexString(b.toInt() and 0xFF or 0x100).substring(1, 3))
        }
        return array to sb.toString()
    }

    /**
     * Called by system. We use this to dispose resources
     */
    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}