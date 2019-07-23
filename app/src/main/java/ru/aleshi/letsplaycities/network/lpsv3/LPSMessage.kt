package ru.aleshi.letsplaycities.network.lpsv3

import ru.aleshi.letsplaycities.base.game.WordResult
import ru.aleshi.letsplaycities.base.player.AuthData
import ru.aleshi.letsplaycities.base.player.PlayerData
import ru.aleshi.letsplaycities.network.AuthType
import ru.aleshi.letsplaycities.network.FriendModeResult
import java.nio.ByteBuffer

sealed class LPSMessage {

    companion object {

        fun from(msgReader: LPSMessageReader): LPSMessage {
            return when (val action = msgReader.getMasterTag()) {
                LPSv3Tags.ACTION_JOIN -> LPSPlayMessage(msgReader)
                LPSv3Tags.ACTION_SYNC -> LPSSyncMessage(msgReader, action)
                LPSv3Tags.S_ACTION_WORD -> LPSWordMessage(msgReader, action)
                LPSv3Tags.S_ACTION_MSG -> LPSMsgMessage(msgReader, action)
                LPSv3Tags.S_ACTION_LEAVE -> LPSLeaveMessage(msgReader, action)
                LPSv3Tags.ACTION_TIMEOUT -> LPSTimeoutMessage
                LPSv3Tags.ACTION_BANNED -> LPSBannedMessage(msgReader, action)
                LPSv3Tags.ACTION_FRIEND_MODE_REQ -> LPSFriendModeRequest(msgReader, action)
                LPSv3Tags.ACTION_FRIEND_REQUEST -> LPSFriendRequest(msgReader, action)
                LPSv3Tags.ACTION_QUERY_FRIEND_RES -> LPSFriendsList(msgReader, action)
                LPSv3Tags.ACTION_REQUEST_FIREBASE -> LPSRequestFirebaseToken
                else -> LPSUnknownMessage
            }
        }
    }

    class LPSPlayMessage internal constructor(msgReader: LPSMessageReader) : LPSMessage() {
        val opponentPlayer = PlayerData()
        var youStarter = false

        init {
            var tag = msgReader.nextTag()

            var userID = 0
            var snUID = "0"
            var snName = "nv"

            while (tag > 0) {
                when (tag) {
                    LPSv3Tags.ACTION_JOIN -> youStarter = msgReader.readBoolean(tag)
                    LPSv3Tags.S_CAN_REC_MSG -> opponentPlayer.canReceiveMessages = msgReader.readBoolean(tag)
                    LPSv3Tags.S_AVATAR_PART0 -> opponentPlayer.avatar = msgReader.readBytes(tag)
                    LPSv3Tags.OPP_LOGIN -> opponentPlayer.userName = msgReader.readString(tag)
                    LPSv3Tags.OPP_CLIENT_VERSION -> opponentPlayer.clientVersion = msgReader.readString(tag)
                    LPSv3Tags.OPP_CLIENT_BUILD -> opponentPlayer.clientBuild = msgReader.readChar(tag)
                    LPSv3Tags.OPP_IS_FRIEND -> opponentPlayer.isFriend = msgReader.readBoolean(tag)
                    LPSv3Tags.S_OPP_UID -> userID = msgReader.readInt(tag)
                    LPSv3Tags.S_OPP_SN -> snName = AuthType.values()[msgReader.readByte(tag).toInt()].type()
                    LPSv3Tags.S_OPP_SNUID -> snUID = msgReader.readString(tag)
                }
                tag = msgReader.nextTag()
            }
            opponentPlayer.authData =
                AuthData(opponentPlayer.userName!!, snUID, snName, "").apply { this.userID = userID }
            opponentPlayer.allowSendUID = true
        }

    }

    class LPSSyncMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        init {
            //Skip: time in seconds
            msgReader.readChar(action)
        }
    }

    class LPSWordMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val result = WordResult.values()[msgReader.readByte(action).toInt()]
        val word = msgReader.readString(LPSv3Tags.WORD)
    }

    class LPSMsgMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val message = msgReader.readString(action)
        val isSystemMsg = msgReader.readBoolean(LPSv3Tags.MSG_OWNER)
    }

    class LPSLeaveMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val leaved = msgReader.readBoolean(action)
    }

    class LPSBannedMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val isBannedBySystem = msgReader.readByte(action) == 2.toByte()
        val description = msgReader.readString(LPSv3Tags.S_BAN_REASON)
    }

    class LPSFriendModeRequest internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val login = msgReader.readString(LPSv3Tags.FRIEND_MODE_REQ_LOGIN)
        val userId = msgReader.readInt(LPSv3Tags.FRIEND_MODE_REQ_UID)
        val result = FriendModeResult.values()[msgReader.readByte(action).toInt()]
    }

    enum class FriendRequest { NEW_REQUEST, ACCEPTED, DENIED }

    class LPSFriendRequest internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val requestResult =
            when (msgReader.readByte(action)) {
                LPSv3Tags.E_NEW_REQUEST ->FriendRequest.NEW_REQUEST
                LPSv3Tags.E_FRIEND_SAYS_YES -> FriendRequest.ACCEPTED
                LPSv3Tags.E_FRIEND_SAYS_NO -> FriendRequest.DENIED
                else -> throw LPSException("Invalid friend request")
            }
    }

    class LPSFriendsList internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val list: ArrayList<FriendsInfo>

        init {
            val size = msgReader.readChar(action)
            val names =
                msgReader.readString(LPSv3Tags.F_QUERY_NAMES).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val accept = msgReader.readBytes(LPSv3Tags.F_QUERY_USER_ACCEPT)
            val userIds = ByteBuffer.wrap(msgReader.readBytes(LPSv3Tags.F_QUERY_USER_IDS))

            list = ArrayList(size)
            for (i in 0 until size) {
                list.add(FriendsInfo(userIds.int, names[i], accept[i] > 0))
            }

            userIds.clear()
        }
    }

    object LPSTimeoutMessage : LPSMessage()

    object LPSRequestFirebaseToken : LPSMessage()

    object LPSUnknownMessage : LPSMessage()
}