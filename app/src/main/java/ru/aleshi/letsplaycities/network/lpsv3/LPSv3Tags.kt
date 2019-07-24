package ru.aleshi.letsplaycities.network.lpsv3

object LPSv3Tags {

    // Magic code
    const val LPS_SERVER_VALID: Int = 0xBEEB
    const val LPS_CLIENT_VALID: Int = 0xEBBE

    // Client Tags

    // Login tags
    const val ACTION_LOGIN: Byte = 2
    const val CLIENT_VERSION: Byte = 3
    const val CLIENT_BUILD: Byte = 4
    const val UUID: Byte = 5
    const val LOGIN: Byte = 6
    const val AVATAR_PART0: Byte = 7
    const val AVATAR_PART1: Byte = 8
    const val CAN_REC_MSG: Byte = 9
    const val UID: Byte = 10// v4
    const val ACC_HASH: Byte = 11// v4
    const val SN: Byte = 12// v4
    const val SN_UID: Byte = 13// v4
    const val ACCESS_TOKEN: Byte = 91// v4
    const val ALLOW_SEND_UID: Byte = 92// v4

    // Play Tags
    const val ACTION_PLAY: Byte = 14
    const val E_RANDOM_PAIR_MODE: Byte = 0
    val E_FRIEND_MODE: Byte = 1
    const val OPP_UID: Byte = 16

    // Message Tags
    const val ACTION_MSG: Byte = 18

    // Word Tags
    const val ACTION_WORD: Byte = 20

    // Ban Tags
    const val ACTION_BAN: Byte = 22
    const val E_USER_BAN: Byte = 0
    const val E_REPORT: Byte = 1
    const val UBAN_REASON: Byte = 23
    const val ROOM_CONTENT: Byte = 24

    // Leave Tags
    const val ACTION_LEAVE: Byte = 26

    // Friend Tags
    const val ACTION_FRIEND: Byte = 28
    const val E_SEND_REQUEST: Byte = 1
    const val E_ACCEPT_REQUSET: Byte = 2
    const val E_DENY_REQUSET: Byte = 3
    const val E_DELETE_REQUEST: Byte = 4
    const val FRIEND_UID: Byte = 30

    // Query friend info Tags
    const val ACTION_QUERY_FRIEND_INFO: Byte = 32
    const val F_REQ_UUID: Byte = 33

    // Friend mode request Tags
    const val ACTION_FM_REQ_RESULT: Byte = 36
    const val SENDER_UID: Byte = 37// LPSv4

    // Query banlist Tags
    const val ACTION_QUERY_BANLIST: Byte = 106
    const val ACTION_QUERY_BANLIST_RES: Byte = 107
    const val E_QUERY_LIST: Byte = 1

    //Firebase request Tags
    const val ACTION_FIREBASE_TOKEN: Byte = 110

    // Server Tags

    // Login tags
    const val ACTION_LOGIN_RESULT: Byte = 38
    const val NEWER_BUILD: Byte = 39
    const val BAN_REASON: Byte = 40
    const val CONNECTION_ERROR: Byte = 41
    const val S_UID: Byte = 42
    const val S_ACC_HASH: Byte = 43
    const val S_API_VERSION: Byte = 100

    // Play Tags
    const val ACTION_JOIN: Byte = 44
    const val BANNED_BY_OPP: Byte = 45
    const val S_CAN_REC_MSG: Byte = 46
    const val S_OPP_UUID: Byte = 47
    const val S_AVATAR_PART0: Byte = 48
    const val S_AVATAR_PART1: Byte = 49
    const val OPP_LOGIN: Byte = 50
    const val OPP_CLIENT_VERSION: Byte = 51
    const val OPP_CLIENT_BUILD: Byte = 52
    const val S_OPP_UID: Byte = 53// v4
    const val S_OPP_SN: Byte = 54// v4
    const val S_OPP_SNUID: Byte = 55// v4
    const val S_BANNED_BY_OPP: Byte = 105
    const val OPP_IS_FRIEND: Byte = 104

    // Word Tags
    const val S_ACTION_WORD: Byte = 58
    const val WORD: Byte = 59

    // Message Tags
    const val S_ACTION_MSG: Byte = 54
    const val MSG_OWNER: Byte = 55

    // Friend Tags
    const val ACTION_FRIEND_REQUEST: Byte = 62
    const val E_NEW_REQUEST: Byte = 0
    const val E_FRIEND_SAYS_YES: Byte = 1
    const val E_FRIEND_SAYS_NO: Byte = 2

    // Ban Tags
    const val ACTION_BANNED: Byte = 64
    const val S_BAN_REASON: Byte = 65

    // Disconnect Tags
    const val S_ACTION_LEAVE: Byte = 68

    // Query friend info result Tags
    const val ACTION_QUERY_FRIEND_RES: Byte = 70
    const val F_QUERY_USER_ACCEPT: Byte = 71// v4
    const val F_QUERY_USER_IDS: Byte = 72// v4
    const val F_QUERY_NAMES: Byte = 73// v4

    // Sync Tags
    const val ACTION_SYNC: Byte = 74

    // Timeout Tags
    const val ACTION_TIMEOUT: Byte = 76

    // Friend Mode Request Tags
    const val ACTION_FRIEND_MODE_REQ: Byte = 78
    const val FRIEND_MODE_REQ_LOGIN: Byte = 79
    const val FRIEND_MODE_REQ_UID: Byte = 81

    const val E_NEW_FM_REQUEST: Byte = 0
    const val E_FM_RESULT_BUSY: Byte = 1
    const val E_FM_RESULT_OFFLINE: Byte = 2
    const val E_FM_RESULT_NOT_FRIEND: Byte = 3
    const val E_FM_RESULT_DENIED: Byte = 4

    // Action spec Tags
    const val ACTION_SPEC: Byte = 120

    // Action Firebase update token
    const val ACTION_REQUEST_FIREBASE: Byte = 124
}