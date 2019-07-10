package ru.aleshi.letsplaycities.network.lpsv3

import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.network.AuthType
import ru.aleshi.letsplaycities.network.FriendModeResult
import ru.aleshi.letsplaycities.network.PlayerData
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACCESS_TOKEN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACC_HASH
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_BAN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_BANNED
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_FIREBASE_TOKEN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_FM_REQ_RESULT
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_FRIEND
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_FRIEND_MODE_REQ
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_FRIEND_REQUEST
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_JOIN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_LOGIN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_LOGIN_RESULT
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_MSG
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_PLAY
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_QUERY_FRIEND_INFO
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_QUERY_FRIEND_RES
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_REQUEST_FIREBASE
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_SYNC
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_TIMEOUT
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ACTION_WORD
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ALLOW_SEND_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.AVATAR_PART0
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.BAN_REASON
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.CAN_REC_MSG
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.CLIENT_BUILD
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.CLIENT_VERSION
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.CONNECTION_ERROR
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_ACCEPT_REQUSET
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_DELETE_REQUEST
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_DENY_REQUSET
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_FRIEND_MODE
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_FRIEND_SAYS_NO
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_FRIEND_SAYS_YES
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_RANDOM_PAIR_MODE
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.E_SEND_REQUEST
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.FRIEND_MODE_REQ_LOGIN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.FRIEND_MODE_REQ_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.FRIEND_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.F_QUERY_NAMES
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.F_QUERY_USER_ACCEPT
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.F_QUERY_USER_IDS
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.LOGIN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.MSG_OWNER
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.NEWER_BUILD
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.OPP_CLIENT_BUILD
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.OPP_CLIENT_VERSION
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.OPP_IS_FRIEND
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.OPP_LOGIN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.OPP_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.ROOM_CONTENT
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.SENDER_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.SN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.SN_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_ACC_HASH
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_ACTION_LEAVE
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_ACTION_MSG
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_ACTION_WORD
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_AVATAR_PART0
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_BAN_REASON
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_CAN_REC_MSG
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_OPP_SN
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_OPP_SNUID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_OPP_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.S_UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.UID
import ru.aleshi.letsplaycities.network.lpsv3.LPSv3Tags.WORD
import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.logging.Logger


class NetworkClient(private val mErrorListener: IErrorListener) {

    //TODO
    enum class PlayState {
        PLAY,
        WAITING,
        CONNECT,
        SERVICE
    }

    @Volatile
    private var mRunningFlag = true
    @Volatile
    private var mFlushingLock = false
    private val mSendingLock = Object()
    private val mTasks = ArrayBlockingQueue<LPSMessageWriter>(10)
    private lateinit var mInputStream: DataInputStream
    private lateinit var mOutputStream: DataOutputStream
    private lateinit var mSocket: Socket
    private lateinit var mLoginListener: ILogInListener
    private lateinit var mUserData: PlayerData
    private var mSendingThread: Thread

    var serviceListener: IServiceListener? = null
    var gameListener: IGameListener? = null
    var kicked = false

    companion object {
        private const val PORT = 62964
        private var HOST = BuildConfig.HOST
        private val LOG = Logger.getLogger(NetworkClient::class.java.simpleName)
        private var instance: NetworkClient? = null

        fun getNetworkClient(): NetworkClient? {
            return instance
        }

        fun createNetworkClient(errorListener: IErrorListener): NetworkClient {
            if (instance != null)
                if (!instance!!.mRunningFlag)
                    LOG.info("Instance != null, but mRunningFlag = false. Creating new instance!")
                else {
                    instance!!.disconnect()
                    LOG.info("createNetworkClient: previous instance not closed!")
                }
            instance = NetworkClient(errorListener)
            return instance!!
        }
    }

    init {
        val sendingRunnable = Runnable {
            while (mRunningFlag) {
                try {
                    sendPendingData()
                } catch (e: IOException) {
                    mErrorListener.onException(e)
                    mRunningFlag = false
                } catch (e: InterruptedException) {
                    mErrorListener.onException(e)
                    mRunningFlag = false
                }

            }
            LOG.info("SendingThread Stopped!")
        }
        mSendingThread = Thread(sendingRunnable, "Sending Thread")
    }

    fun connect(logInListener: ILogInListener, userData: PlayerData, state: PlayState, userId: Int) {
        this.mLoginListener = logInListener
        this.mUserData = userData
        logInListener.onConnect(this, userData, state)

        val networkRunnable = Runnable {
            try {
                val ipAddress = InetAddress.getByName(HOST)
                mSocket = Socket(ipAddress, PORT)

                mInputStream = DataInputStream(BufferedInputStream(mSocket.getInputStream(), 64 * 1024))
                mOutputStream = DataOutputStream(BufferedOutputStream(mSocket.getOutputStream(), 64 * 1024))

                mSendingThread.start()

                logIn()

                when (state) {
                    PlayState.PLAY -> play(false, 0)
                    PlayState.WAITING -> play(true, userId)
                    PlayState.CONNECT -> logInListener.onFriendModeRequest(FriendModeResult.REQUEST, null, userId)
                    PlayState.SERVICE -> {
                    }
                }

                while (mRunningFlag) {
                    // Waiting for message
                    val msgReader: LPSMessageReader
                    try {
                        msgReader = LPSMessageReader(mInputStream)
                        handleMessage(msgReader)
                    } catch (e: LPSException) {
                        e.printStackTrace()
                        mErrorListener.onInvalidMessage()
                        mRunningFlag = false
                    }

                }

            } catch (ex: java.lang.Exception) {
                if (mRunningFlag)
                    mErrorListener.onException(ex)
                mRunningFlag = false
            } finally {
                disconnect()
            }
            LOG.info("NetworkThread Stopped!")
        }

        Thread(networkRunnable, "NetworkGameThread").start()
    }

    @Throws(IOException::class)
    private fun logIn() {
        val ad = mUserData.authData!!

        val msgWriter = LPSMessageWriter(mOutputStream)
        msgWriter.writeByte(ACTION_LOGIN, 4)
        msgWriter.writeString(LOGIN, mUserData.userName!!)

        if (ad.userID > 0) msgWriter.writeInt(UID, ad.userID)
        if (ad.accessHash != null) msgWriter.writeString(ACC_HASH, ad.accessHash!!)
        if (ad.accessToken != null) msgWriter.writeString(ACCESS_TOKEN, ad.accessToken!!)
        msgWriter.writeByte(SN, ad.snType.ordinal.toByte())
        if (ad.snUID != null) msgWriter.writeString(SN_UID, ad.snUID!!)

        msgWriter.writeChar(CLIENT_BUILD, mUserData.clientBuild)
        msgWriter.writeString(CLIENT_VERSION, mUserData.clientVersion)
        msgWriter.writeBool(CAN_REC_MSG, mUserData.canReceiveMessages)
        msgWriter.writeBool(ALLOW_SEND_UID, mUserData.allowSendUID)

        val imageSize = if (mUserData.avatar == null) 0 else mUserData.avatar!!.size
        if (imageSize > 0) {
            // NOTE: Now we can write only 64kB images
            msgWriter.writeBytes(AVATAR_PART0, mUserData.avatar!!)
        }

        msgWriter.buildAndFlush()

        val msgReader = LPSMessageReader(mInputStream)

        if (msgReader.readBoolean(ACTION_LOGIN_RESULT)) {
            ad.userID = msgReader.readInt(S_UID)
            ad.accessHash = msgReader.readString(S_ACC_HASH)
            mLoginListener.onLoggedIn(ad)
            val newerBuild = msgReader.readChar(NEWER_BUILD)
            if (mUserData.clientBuild < newerBuild) {
                mLoginListener.onNewerBuildAvailable()
            }
            return
        }

        val reason = msgReader.readString(BAN_REASON)
        val connError = msgReader.readString(CONNECTION_ERROR)
        mLoginListener.onLoginFailed(reason, connError!!)
        mRunningFlag = false
    }

    private fun handleMessage(msgReader: LPSMessageReader) {
        when (val action = msgReader.getMasterTag()) {
            ACTION_JOIN -> handlePlay(msgReader)
            ACTION_SYNC -> {
                val time = msgReader.readChar(action) * 1000
                gameListener?.onSync(time)
            }
            S_ACTION_WORD -> {
                val stat = msgReader.readByte(action).toInt()
                val word = msgReader.readString(WORD)
                gameListener?.onWord(WordResult.values()[stat], word!!)
            }
            S_ACTION_MSG -> {
                val msg = msgReader.readString(action)
                gameListener?.run {
                    val userMsg = msgReader.readBoolean(MSG_OWNER)
                    onMessage(userMsg, msg!!)
                }
            }
            S_ACTION_LEAVE -> if (gameListener != null) {
                val leaved = msgReader.readBoolean(action)
                gameListener?.onDisconnected(leaved)
            }

            ACTION_TIMEOUT -> if (gameListener != null)
                gameListener?.onTimeOut()

            ACTION_BANNED -> {
                kicked = true
                val type = msgReader.readByte(action).toInt()
                val desc = msgReader.readString(S_BAN_REASON)
                mLoginListener.onKicked(type == 2, desc!!)
            }

            ACTION_FRIEND_MODE_REQ -> {
                val resultCode = msgReader.readByte(action).toInt()
                val login = msgReader.readString(FRIEND_MODE_REQ_LOGIN)
                val userId = msgReader.readInt(FRIEND_MODE_REQ_UID)
                val result =
                    try {
                        FriendModeResult.values()[resultCode]
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                if (result != null)
                    mLoginListener.onFriendModeRequest(result, login, userId)
            }
            ACTION_FRIEND_REQUEST -> {
                val friendAction =
                    when (msgReader.readByte(action)) {
                        E_FRIEND_SAYS_YES -> "y"
                        E_FRIEND_SAYS_NO -> "n"
                        else -> throw LPSException("Invalid friend request")
                    }
                gameListener?.onFriendsAction(friendAction)
            }
            ACTION_QUERY_FRIEND_RES -> handleFriendsList(msgReader)

            ACTION_REQUEST_FIREBASE -> mLoginListener.onRequestFirebaseToken()
            else -> gameListener?.onServerCommand(action)
        }
    }

    private fun handlePlay(msg: LPSMessageReader) {
        val opp = PlayerData()

        var youStarter = false
        var tag = msg.nextTag()
        while (tag > 0) {
            when (tag.toByte()) {
                ACTION_JOIN -> youStarter = msg.readBoolean(tag.toByte())
                S_CAN_REC_MSG -> opp.canReceiveMessages = msg.readBoolean(tag.toByte())
                S_AVATAR_PART0 -> opp.avatar = msg.readBytes(tag.toByte())
                S_OPP_UID -> opp.authData!!.userID = msg.readInt(tag.toByte())
                S_OPP_SN -> opp.authData!!.snType = AuthType.values()[msg.readByte(tag.toByte()).toInt()]
                S_OPP_SNUID -> opp.authData!!.snUID = msg.readString(tag.toByte())
                OPP_LOGIN -> opp.userName = msg.readString(tag.toByte())
                OPP_CLIENT_VERSION -> opp.clientVersion = msg.readString(tag.toByte())!!
                OPP_CLIENT_BUILD -> opp.clientBuild = msg.readChar(tag.toByte())
                OPP_IS_FRIEND -> opp.isFriend = msg.readBoolean(tag.toByte())
            }
            tag = msg.nextTag()
        }
        opp.allowSendUID = opp.authData!!.snUID != null
        mLoginListener.onPlay(opp, youStarter)
    }

    private fun handleFriendsList(msgReader: LPSMessageReader) {
        if (serviceListener == null)
            return
        val size = msgReader.readChar(ACTION_QUERY_FRIEND_RES)
        val list = ArrayList<FriendsInfo>(size)
        val names = msgReader.readString(F_QUERY_NAMES)!!.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val accept = msgReader.readBytes(F_QUERY_USER_ACCEPT)
        val userIds = ByteBuffer.wrap(msgReader.readBytes(F_QUERY_USER_IDS)!!)

        for (i in 0 until size) {
            list.add(FriendsInfo(userIds.int, names[i], accept!![i] > 0))
        }

        userIds.clear()
        serviceListener!!.onFriendsList(list)
    }

    @Throws(IOException::class)
    private fun play(waiting: Boolean, userId: Int) {
        val msg = LPSMessageWriter(mOutputStream)
        msg.writeByte(ACTION_PLAY, if (waiting) E_FRIEND_MODE else E_RANDOM_PAIR_MODE)
        msg.writeInt(OPP_UID, userId)
        msg.buildAndFlush()
    }

    fun sendWord(word: String) {
        sendAndNotify(LPSMessageWriter(mOutputStream).writeString(ACTION_WORD, word))
    }

    fun sendMessage(msg: String) {
        sendAndNotify(LPSMessageWriter(mOutputStream).writeString(ACTION_MSG, msg))
    }

    fun sendFriendRequest() {
        sendAndNotify(LPSMessageWriter(mOutputStream).writeByte(ACTION_FRIEND, E_SEND_REQUEST))
    }

    fun sendFriendAcceptance(accepted: Boolean) {
        sendAndNotify(
            LPSMessageWriter(mOutputStream).writeByte(
                ACTION_FRIEND,
                if (accepted) E_ACCEPT_REQUSET else E_DENY_REQUSET
            )
        )
    }

    fun sendFriendDeletion(userId: Int) {
        sendAndNotify(
            LPSMessageWriter(mOutputStream).writeByte(ACTION_FRIEND, E_DELETE_REQUEST).writeInt(
                FRIEND_UID,
                userId
            )
        )
    }

    fun sendFirebaseToken(firebaseToken: String) {
        sendAndNotify(LPSMessageWriter(mOutputStream).writeString(ACTION_FIREBASE_TOKEN, firebaseToken))
    }

    fun sendGetFriendsMsg() {
        sendAndNotify(LPSMessageWriter(mOutputStream).writeByte(ACTION_QUERY_FRIEND_INFO, 1))
    }

    fun sendRequestResult(result: Boolean, userId: Int) {
        sendAndNotify(
            LPSMessageWriter(mOutputStream).writeByte(ACTION_FM_REQ_RESULT, (if (result) 1 else 2).toByte()).writeInt(
                SENDER_UID,
                userId
            )
        )
    }

    private fun sendAndNotify(msgWriter: LPSMessageWriter) {
        mTasks.add(msgWriter)
        synchronized(mSendingLock) {
            mSendingLock.notifyAll()
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun sendPendingData() {
        if (mTasks.isEmpty()) {
            synchronized(mSendingLock) {
                mSendingLock.wait()
            }
        }

        val task = mTasks.poll() ?: return

        mFlushingLock = true
        task.buildAndFlush()
        mFlushingLock = false
    }

    fun kick() {
        sendAndNotify(
            LPSMessageWriter(mOutputStream).writeByte(ACTION_BAN, 0).writeString(BAN_REASON, "unimpl")
                .writeString(ROOM_CONTENT, "unimpl")
        )
        while (!mTasks.isEmpty() || mFlushingLock) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
        disconnect()
    }

    fun disconnect() {
        mRunningFlag = false
        synchronized(mSendingLock) {
            mSendingLock.notifyAll()
        }
        forceClose()
    }

    private fun forceClose() {
        try {
            mInputStream.close()
        } catch (e: Exception) {
            // Empty catch block
        }

        try {
            mOutputStream.close()
        } catch (e: Exception) {
            // Empty catch block
        }

        try {
            mSocket.close()
        } catch (e: Exception) {
            // Empty catch block
        }

    }
}