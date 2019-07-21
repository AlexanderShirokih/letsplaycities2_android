package ru.aleshi.letsplaycities.network.lpsv3

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.base.player.AuthData
import ru.aleshi.letsplaycities.network.AuthType
import ru.aleshi.letsplaycities.network.NetworkUtils
import ru.aleshi.letsplaycities.network.PlayerData
import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer

class NetworkClient2 {

    companion object {
        private const val PORT = 62964
        const val HOST = BuildConfig.HOST
    }

    private lateinit var mInputStream: DataInputStream
    private lateinit var mOutputStream: DataOutputStream
    private lateinit var mSocket: Socket

    class AuthResult(
        val authData: AuthData,
        val newerBuild: Int
    )

    @Throws(IOException::class)
    fun connect() {
        val ipAddress = InetAddress.getByName(HOST)
        mSocket = Socket(ipAddress, PORT)

        mInputStream = DataInputStream(BufferedInputStream(mSocket.getInputStream(), 64 * 1024))
        mOutputStream = DataOutputStream(BufferedOutputStream(mSocket.getOutputStream(), 64 * 1024))
    }

    fun disconnect() {
        try {
            if (::mInputStream.isInitialized)
                mInputStream.close()
        } catch (e: IOException) {
        }
        try {
            if (::mOutputStream.isInitialized)
                mOutputStream.close()
        } catch (e: IOException) {
        }
        try {
            if (::mSocket.isInitialized)
                mSocket.close()
        } catch (e: IOException) {
        }
    }

    @Throws(AuthorizationException::class)
    fun login(userData: PlayerData): AuthResult {
        val ad = userData.authData!!

        val msgWriter = LPSMessageWriter(mOutputStream)
            .writeByte(LPSv3Tags.ACTION_LOGIN, 4)
            .writeString(LPSv3Tags.LOGIN, userData.userName!!)
            .writeString(LPSv3Tags.ACCESS_TOKEN, ad.accessToken)
            .writeByte(LPSv3Tags.SN, ad.getSnType().ordinal.toByte())
            .writeString(LPSv3Tags.SN_UID, ad.snUID)
            .writeChar(LPSv3Tags.CLIENT_BUILD, userData.clientBuild)
            .writeString(LPSv3Tags.CLIENT_VERSION, userData.clientVersion)
            .writeBool(LPSv3Tags.CAN_REC_MSG, userData.canReceiveMessages)
            .writeBool(LPSv3Tags.ALLOW_SEND_UID, userData.allowSendUID)

        if (ad.userID > 0) msgWriter.writeInt(LPSv3Tags.UID, ad.userID)
        if (ad.accessHash != null) msgWriter.writeString(LPSv3Tags.ACC_HASH, ad.accessHash!!)
        // NOTE: Now we can write only 64kB images
        userData.avatar?.run { if (this.isNotEmpty()) msgWriter.writeBytes(LPSv3Tags.AVATAR_PART0, userData.avatar!!) }

        msgWriter.buildAndFlush()

        val msgReader = LPSMessageReader(mInputStream)

        if (msgReader.readBoolean(LPSv3Tags.ACTION_LOGIN_RESULT)) {
            ad.userID = msgReader.readInt(LPSv3Tags.S_UID)
            ad.accessHash = msgReader.readString(LPSv3Tags.S_ACC_HASH)
            val newerBuild = msgReader.readChar(LPSv3Tags.NEWER_BUILD)
            return AuthResult(ad, newerBuild)
        }

        val reason = msgReader.optString(LPSv3Tags.BAN_REASON)
        val connError = msgReader.optString(LPSv3Tags.CONNECTION_ERROR)
        throw AuthorizationException(reason, connError)
    }

    @Throws(IOException::class)
    fun play(isWaiting: Boolean, userId: Int?): Pair<PlayerData, Boolean> {
        LPSMessageWriter(mOutputStream)
            .writeByte(LPSv3Tags.ACTION_PLAY, if (isWaiting) LPSv3Tags.E_FRIEND_MODE else LPSv3Tags.E_RANDOM_PAIR_MODE)
            .writeInt(LPSv3Tags.OPP_UID, if (isWaiting) userId!! else 0)
            .buildAndFlush()

        val msg = readAndCheckForFirebaseToken()
        var tag = msg.nextTag()

        val opp = PlayerData()
        var youStarter = false
        var userID = 0
        var snUID = "0"
        var snName = "nv"

        while (tag > 0) {
            when (tag) {
                LPSv3Tags.ACTION_JOIN -> youStarter = msg.readBoolean(tag)
                LPSv3Tags.S_CAN_REC_MSG -> opp.canReceiveMessages = msg.readBoolean(tag)
                LPSv3Tags.S_AVATAR_PART0 -> opp.avatar = msg.readBytes(tag)
                LPSv3Tags.OPP_LOGIN -> opp.userName = msg.readString(tag)
                LPSv3Tags.OPP_CLIENT_VERSION -> opp.clientVersion = msg.readString(tag)
                LPSv3Tags.OPP_CLIENT_BUILD -> opp.clientBuild = msg.readChar(tag)
                LPSv3Tags.OPP_IS_FRIEND -> opp.isFriend = msg.readBoolean(tag)
                LPSv3Tags.S_OPP_UID -> userID = msg.readInt(tag)
                LPSv3Tags.S_OPP_SN -> snName = AuthType.values()[msg.readByte(tag).toInt()].type()
                LPSv3Tags.S_OPP_SNUID -> snUID = msg.readString(tag)
            }
            tag = msg.nextTag()
        }
        opp.authData = AuthData(opp.userName!!, snUID, snName, "")
            .apply { this.userID = userID }
        opp.allowSendUID = true
        return opp to youStarter
    }

    private fun readAndCheckForFirebaseToken(): LPSMessageReader {
        val msg = LPSMessageReader(mInputStream)
        if (msg.getMasterTag() == LPSv3Tags.ACTION_REQUEST_FIREBASE) {
            updateToken()
            return LPSMessageReader(mInputStream)
        }
        return msg
    }

    fun getFriendsList(): ArrayList<FriendsInfo> {
        LPSMessageWriter(mOutputStream)
            .writeByte(LPSv3Tags.ACTION_QUERY_FRIEND_INFO, 1)
            .buildAndFlush()

        val msgReader = LPSMessageReader(mInputStream)

        val size = msgReader.readChar(LPSv3Tags.ACTION_QUERY_FRIEND_RES)
        val list = ArrayList<FriendsInfo>(size)
        val names =
            msgReader.readString(LPSv3Tags.F_QUERY_NAMES).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val accept = msgReader.readBytes(LPSv3Tags.F_QUERY_USER_ACCEPT)
        val userIds = ByteBuffer.wrap(msgReader.readBytes(LPSv3Tags.F_QUERY_USER_IDS))

        for (i in 0 until size) {
            list.add(FriendsInfo(userIds.int, names[i], accept[i] > 0))
        }

        userIds.clear()
        return list
    }

    fun deleteFriend(userId: Int) {
        LPSMessageWriter(mOutputStream)
            .writeByte(LPSv3Tags.ACTION_FRIEND, LPSv3Tags.E_DELETE_REQUEST)
            .writeInt(LPSv3Tags.FRIEND_UID, userId)
            .buildAndFlush()
    }

    fun isConnected(): Boolean {
        return ::mSocket.isInitialized && mSocket.isConnected && !mSocket.isClosed
    }

    private fun updateToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(NetworkUtils::class.java.simpleName, "FirebaseInstanceId.getInstance() failed", task.exception)
            } else {
                //174 chars
                Completable.fromAction {
                    LPSMessageWriter(mOutputStream)
                        .writeString(LPSv3Tags.ACTION_FIREBASE_TOKEN, task.result!!.token)
                        .buildAndFlush()
                }.subscribeOn(Schedulers.io())
                    .subscribe()
            }
        }
    }
}