package ru.aleshi.letsplaycities.base


class BanManager(private val prefs: GamePreferences) {

    companion object {
        private const val ATTR_UUID = "uuid"
        private const val ATTR_LOGIN = "logn"
    }

    fun addToBanList(login: String, userId: Int) {
        val list = getBanList()
        list.add("$userId=$login")
        val string = getStringFromList(list)
        list.clear()
        prefs.putBanned(string)
    }

    fun removeFromBanList(pos: Int) {
        val list = getBanList()
        list.removeAt(pos)
        val string = getStringFromList(list)
        list.clear()
        prefs.putBanned(string)
    }

    fun checkInBanList(userId: Int): Boolean {
        val userIdAsString = userId.toString()
        for (ln in getBanList()) {
            if (ln.contains(userIdAsString)) {
                return true
            }
        }
        return false
    }

    fun getBannedPlayersNameList(): List<String> {
        return getBannedPlayersList().map { it[ATTR_LOGIN].toString() }
    }

    private fun getBannedPlayersList(): MutableList<Map<String, Any>> {
        val bl = getBanList()
        val bannedPlayers = mutableListOf<Map<String, Any>>()

        for (item in bl) {
            val itm = mutableMapOf<String, Any>()
            val split = item.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            itm[ATTR_UUID] = split[0]
            itm[ATTR_LOGIN] = if (split.size > 1) split[1] else "User ID: " + split[0]
            bannedPlayers.add(itm)
        }
        return bannedPlayers
    }

    private fun getBanList(): MutableList<String> {
        val ban = prefs.getBanned()
        if (ban.isEmpty())
            return mutableListOf()
        val items = ban.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return items.toMutableList()
    }

    private fun getStringFromList(list: MutableList<String>): String {
        val builder = StringBuilder()
        for (i in 0 until list.size) {
            builder.append(list[i])
            if (i < list.size - 1)
                builder.append('|')
        }
        return builder.toString()
    }
}