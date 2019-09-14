package ru.aleshi.letsplaycities.remote.internal

import ru.quandastudio.lpsclient.core.LPSMessageReader
import ru.quandastudio.lpsclient.core.LPSv3Tags
import ru.quandastudio.lpsclient.model.PlayerData

sealed class LPSServerMessage {

    companion object {
        fun from(msgReader: LPSMessageReader): LPSServerMessage {
            return when (val action = msgReader.getMasterTag()) {
                LPSv3Tags.ACTION_WORD -> LPSWordServerMessage(
                    msgReader,
                    action
                )
                LPSv3Tags.ACTION_MSG -> LPSMsgServerMessage(
                    msgReader,
                    action
                )
                else -> LPSUnknownServerMessage
            }
        }
    }

    class LPSWordServerMessage internal constructor(val word: String) :
        LPSServerMessage() {
        constructor(msgReader: LPSMessageReader, action: Byte) : this(msgReader.readString(action))
    }

    class LPSMsgServerMessage internal constructor(val message: String) : LPSServerMessage() {
        constructor(msgReader: LPSMessageReader, action: Byte) : this(msgReader.readString(action))
    }

    data class LPSConnectedMessage(val opponentData: PlayerData) : LPSServerMessage()

    class LPSLeaveServerMessage(val message: String?) : LPSServerMessage()

    object LPSUnknownServerMessage : LPSServerMessage()
}