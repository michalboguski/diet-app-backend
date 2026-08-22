package pl.edu.pjwstk.s25236.dietapp.processing

class IngredientNameNormalizer {
    val dictionary: Map<String, String> = loadInflectionDictionary()

    fun loadInflectionDictionary(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val inputStream =
            this::class.java.getResourceAsStream("/odm.txt")
                ?: throw IllegalStateException("Cannot find odm.txt in classpath")

        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { rawLine ->
                rawLine
                    .split(',')
                    .map { it.trim() }
                    .map { it.lowercase() }
                    .filter { it.isNotEmpty() }
                    .also { tokens ->
                        if (tokens.isNotEmpty()) {
                            val base = tokens.first()
                            tokens.forEach { form ->
                                map[form.lowercase()] = base
                            }
                        }
                    }
            }
        }
        println("Dictionary created with ${map.size} entries")
        return map.toMap()
    }

    fun base(parsed: String): String = dictionary[parsed] ?: parsed.stripSuffix()

    fun normalizePlForMatching(text: String): List<String> =
        text
            .lowercase()
            .trim()
            .split(Regex("[^\\p{L}0-9]+"))
            .asSequence()
            .filter { it.isNotBlank() }
            .map { base(it) }
            .map { it.trim() }
            .filter { it.length > 1 }
            .toList()
}

fun String.stripSuffix(): String =
    when {
        length <= 4 -> this
        endsWith("owej") -> dropLast(4)
        endsWith("ami") -> dropLast(3)
        endsWith("ach") -> dropLast(3)
        endsWith("ego") -> dropLast(3)
        endsWith("ej") -> dropLast(2)
        endsWith("ów") -> dropLast(2)
        else -> this
    }
