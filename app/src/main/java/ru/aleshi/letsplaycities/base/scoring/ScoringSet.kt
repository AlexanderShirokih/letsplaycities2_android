package ru.aleshi.letsplaycities.base.scoring


class ScoringSet(size: Int) {
    private val allGroups: Array<ScoringGroup?> = arrayOfNulls(size)

    fun storeStatsGroups(): String {
        val stringBuilder = StringBuilder()
        for (i in allGroups.indices) {
            stringBuilder.append(allGroups[i]?.toStringBuilder())
            if (i != allGroups.size - 1)
                stringBuilder.append(',')
        }
        return stringBuilder.toString()
    }

    fun getSize(): Int = allGroups.size

    fun getGroupAt(i: Int) = allGroups[i]!!

    fun getGroupAt(key: String): ScoringGroup {
        for (m in allGroups)
            if (m?.main?.name == key)
                return m
        throw RuntimeException("Requested group by key $key not found")
    }


    fun set(pos: Int, group: ScoringGroup) {
        allGroups[pos] = group
    }

    fun set(key: String, main: ScoringField, child: Array<ScoringField>) {
        getGroupAt(key).apply {
            this.main.copyValue(main)
            child.forEach { this.findField(it.name).copyValue(it) }
        }
    }

    fun loadFromString(s: String) {
        val groups = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (i in groups.indices) {
            val group = groups[i].trim { it <= ' ' }.replace(">", "")
            val main = parseField(
                group.substring(
                    0,
                    group.indexOf('<')
                )
            )
            val fields =
                group.substring(group.indexOf('<') + 1).split("\\|".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            val scoringFields = Array(fields.size) {
                parseField(
                    fields[it]
                )
            }
            set(main.name, main, scoringFields)
        }
    }

    private fun parseField(s: String): ScoringField {
        val eq = s.indexOf('=')
        if (eq < 0)
            return ScoringField(s)
        val name = s.substring(0, eq)
        var value: Any = s.substring(eq + 1)
        value = try {
            Integer.parseInt(value as String)
        } catch (e: NumberFormatException) {
            value
        }
        return ScoringField(name, value)
    }
}