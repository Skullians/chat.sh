package net.skullian.chatsh.expansion.pattern

/**
 * This is a class.
 *
 * @author Skullians
 * @since 12/03/2026
 */
object GlobPattern {

    fun compile(glob: String): CompiledGlob {
        val regex = buildString {
            append("(?i)")
            var i = 0
            while (i < glob.length) {
                when (val c = glob[i]) {
                    '*' -> append(".*")
                    '?' -> append(".")
                    '[' -> {
                        val end = glob.indexOf(']', i)
                        if (end < 0) {
                            append(Regex.escape(c.toString()))
                        } else {
                            val charClass = glob.substring(i + 1, end)
                            val negated = charClass.startsWith("!")
                            val inner = if (negated) charClass.substring(1) else charClass
                            append("[")
                            if (negated) append("^")
                            append(inner)
                            append("]")
                            i = end // increased below
                        }
                    }
                    else -> append(Regex.escape(c.toString()))
                }
                i++
            }
        }
        return CompiledGlob(Regex(regex))
    }

    class CompiledGlob(private val regex: Regex) {
        fun matches(input: String): Boolean = regex.matches(input)
    }
}