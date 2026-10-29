package pl.edu.pjwstk.s25236.dietapp.processing

import pl.edu.pjwstk.s25236.dietapp.db.ScraperDb
import pl.edu.pjwstk.s25236.dietapp.db.ScraperRepository
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe

class RecipeProcessor {
    val cleaner = RecipeCleaner()
    val parser = IngredientLineParser()
    val normalizer = IngredientNameNormalizer()

    val ingredientNormalizationMap: Map<String, String> = mapOf(
        "mąka ziemniaczany" to "Skrobia ziemniaczany",
        "jajo" to "jajko kurzy",
        "jajko" to "jajko kurzy",
        "mąka przenny" to "mąka pszenny typ 550",
        "mąki" to "mąka pszenny typ 550",
        "mąka" to "mąka pszenny typ 550",
        "olej" to "olej rzepakowy",
        "masło" to "masło najwyższy jakość",
        "mleko" to "mleko pełnotłusty świeży",
        "śmietany" to "śmietana",
        "białko" to "białko jajo kurzy",
        "ghee" to "klarować",
        "masy kajmakowy" to "mleko skondensowany słodzony",
        "marchewki" to "marchew",
        "sól" to "sól kamienny",
        "serek typ philadelphia" to "mascarpone"
    )

    //   val recipeRepository = RecipeRepository()
    val scraperRepository = ScraperRepository(ScraperDb.dataSource)

    fun process(raw: RawRecipe): Double {
        var matchCount = 0.0
        raw.ingredientLines.forEach { line -> println(line) }
        val cleanedLines: List<String> = cleaner.clean(raw.ingredientLines)
        val parsedIngredients: List<ParsedIngredient> = parser.parse(cleanedLines)
        parsedIngredients.forEach {
            val normalized = normalizer.normalizePlForMatching(it.candidates)
            val norm = normalizeIngredient(normalized, ingredientNormalizationMap)
            val match = findMatch(norm)
            matchCount += match.second
            println("${it.amount} ${it.measurement.name} ${it.candidates} norm: $norm  |  ${match.first?.name ?: "not fount in db"} | score: ${match.second}")
        }

        return matchCount / parsedIngredients.size
        // return matchCount.toDouble() / raw.ingredientLines.size

        // dla każdej lini składnika
        // normalizacja, do max 5 słów
        // dla każdego słowa wyszukujemy matcha w bazie i pobieramy ingredient
        // dodajemy go do właściwej mapy z jednostką i ilością - specjalny ingredient

        // zapisujemy przepis

        // create recipe
        // save recipe
        //    }

        // mamy liste składników teraz mamy albo i nie ilość, jednostke i
    }

    fun normalizeIngredient(
        list: List<String>,
        normalizationMap: Map<String, String>
    ): List<String> {
        val name = list.joinToString(" ")
        if (normalizationMap.isEmpty()) return name.split(" ").toList()

        val replacements = normalizationMap.entries.associate {
            it.key.lowercase() to it.value
        }

        val alternatives = normalizationMap.keys
            .sortedByDescending { it.length }
            .joinToString("|") { Regex.escape(it) }

        val regex = Regex(
            """(?<![\p{L}\p{N}])(?:$alternatives)(?![\p{L}\p{N}])""",
            RegexOption.IGNORE_CASE
        )

        return regex.replace(name) { match ->
            replacements[match.value.lowercase()] ?: match.value
        }.split(" ").toList()
    }

    // to do
    fun transformName(line: String): List<String> {
        return line.split(" ")
    }


    fun generatePhrases(tokens: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (size in tokens.size downTo 1) {
            for (i in 0..tokens.size - size) {
                result += tokens.subList(i, i + size).joinToString(" ")
            }
        }
        return result
    }

    fun findMatch(tokens: List<String>): Pair<IngredientMatch?, Double> {
        var bestScore: Double = 0.0
        if (tokens.isEmpty()) return null to bestScore
        val wordsCount = tokens.size
        val phrases = generatePhrases(tokens).toSet()
        var bestMatch: IngredientMatch? = null

        for (phrase in phrases) {
            val phaseCount = phrase.split(" ").size
            val coverage = phaseCount.toDouble() / tokens.size
            val minScore =
                when (wordsCount) {
                    1 -> 0.80
                    2 -> 0.65
                    else -> 0.5
                }
            val matches = scraperRepository.findByTrigram(phrase, minScore)
            for (match in matches) {
                val weightedScore = match.score * coverage
                if (bestMatch == null || weightedScore > bestScore) {
                    bestScore = weightedScore
                    bestMatch = match
                }
            }
            if (bestScore > 0.99) break
        }
        return bestMatch to bestScore
    }
}

data class IngredientMatch(
    val ingredientId: Long,
    val name: String,
    val score: Double,
)
