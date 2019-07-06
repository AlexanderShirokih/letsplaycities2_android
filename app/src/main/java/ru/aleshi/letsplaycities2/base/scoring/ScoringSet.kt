package ru.aleshi.letsplaycities2.base.scoring


class ScoringSet(size: Int) {
    private val allGroups: Array<ScoringGroup?> = arrayOfNulls(size)

    fun storeStatsGroups(): String {
        val stringBuilder = StringBuilder()
        for (i in 0 until allGroups.size) {
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

    companion object {
        fun fromString(s: String): ScoringSet {
            val groups = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val set = ScoringSet(groups.size)
            for (i in groups.indices) {
                val t = groups[i].trim { it <= ' ' }.replace(">", "")
                val main = parseField(
                    t.substring(
                        0,
                        t.indexOf('<')
                    )
                )
                val fields =
                    t.substring(t.indexOf('<') + 1).split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val scoringFields = Array(fields.size) {
                    parseField(
                        fields[it]
                    )
                }
                set.set(i, ScoringGroup(main, scoringFields))
            }
            return set
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
                //Ignore
                0
            }
            return ScoringField(name, value)
        }
    }

}