package ru.aleshi.letsplaycities.base.scoring

class ScoringGroup(var main: ScoringField, var child: Array<ScoringField>) {

    fun findField(key: String): ScoringField {
        return child.firstOrNull { it.name == key }
            ?: throw RuntimeException("Requested field $key not found!")
    }

    fun toStringBuilder(): StringBuilder {
        val sb = StringBuilder()
        sb.append(main.name)
        if (main.value != null) {
            sb.append('=')
            sb.append(main.value)
        }
        sb.append('<')
        for (i in child.indices) {
            val field = child[i]
            if (field.value != null) {
                sb.append(field.name)
                sb.append('=')
                sb.append(field.value)

                if (i != child.size - 1) {
                    sb.append('|')
                }
            }
        }
        sb.append('>')
        return sb
    }

}