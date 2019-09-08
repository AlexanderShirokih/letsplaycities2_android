package ru.aleshi.letsplaycities.remote

import ru.quandastudio.lpsclient.core.LPSMessageReader
import ru.quandastudio.lpsclient.core.LPSv3Tags
import ru.quandastudio.lpsclient.model.PlayerData

sealed class LPSServerMessage {

    companion object {
        fun from(msgReader: LPSMessageReader): LPSServerMessage {
            return when (val action = msgReader.getMasterTag()) {
                LPSv3Tags.ACTION_WORD -> LPSWordServerMessage(msgReader, action)
                LPSv3Tags.ACTION_MSG -> LPSMsgServerMessage(msgReader, action)
                else -> LPSUnknownServerMessage
            }
        }
    }

    class LPSWordServerMessage internal constructor(msgReader: LPSMessageReader, action: Byte) :
        LPSServerMessage() {
        val word = msgReader.readString(action)
    }

    class LPSMsgServerMessage internal constructor(msgReader: LPSMessageReader, action: Byte) :
        LPSServerMessage() {
        val message = msgReader.readString(action)
    }

    class LPSConnectedMessage(val opponentData: PlayerData) : LPSServerMessage()

    class LPSDisconnectServerMessage(val message: String?) : LPSServerMessage()

    object LPSUnknownServerMessage : LPSServerMessage()
}