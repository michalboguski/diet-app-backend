package pl.edu.pjwstk.s25236.dietapp.processing

import pl.edu.pjwstk.s25236.dietapp.db.ScraperDb
import pl.edu.pjwstk.s25236.dietapp.db.ScraperRepository
import pl.edu.pjwstk.s25236.dietapp.processing.ProcessResult.ProcessError.ProcessErrorType
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult.RecipeError
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult.RecipeError.RecipeErrorType
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe
import kotlin.collections.toSet

class RecipeProcessor {
    val cleaner = RecipeCleaner()
    val parser = IngredientLineParser()
    val normalizer = IngredientNameNormalizer()
    private val minAcceptedWeightedScore = 0.40

    val trigramCache = mutableMapOf<Pair<String, Double>, ProcessResult>()

    var cacheHits = 0L
    var cacheMisses = 0L

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

    fun process(raw: RawRecipe): RecipeResult {
        val cleanedLines: List<String> = cleaner.cleanLines(raw.ingredientLines)
        val parsedIngredients: List<ParseResult> = parser.parse(cleanedLines)
        val total = cleanedLines.size.toLong()
        var errors = 0L
        var parsed = 0L
        var processed = 0L
        parsedIngredients.forEach {
            when (it) {
                is ParseResult.ParseSuccess -> {
                    parsed++
                }

                is ParseResult.ParseError -> {
                    errors++
                }
            }
        }
        if (errors > 0L || total != parsed) return RecipeError(total, errors, RecipeErrorType.PARSE)

        val processResults = processParsedIngredients(parsedIngredients)
        processResults.forEach {
            when (it) {
                is ProcessResult.ProcessError -> {
                    errors++
                }

                is ProcessResult.IngredientMatch -> {
                    processed++
                }
            }
        }
        if (errors > 0L) return RecipeError(total, errors, RecipeErrorType.PROCESS)

        return if (total == processed) RecipeResult.RecipeSuccess(
            total,
            processResults.map { it as ProcessResult.IngredientMatch }.map { it.ingredientId }.toList()
        ) else RecipeError(total, errors, RecipeErrorType.RECIPE)
    }

    fun processParsedIngredients(parsedIngredients: List<ParseResult>): List<ProcessResult> {
        return parsedIngredients.map { processParsedIngredient(it) }
            .toList()
    }

    fun processParsedIngredient(parsedIngredient: ParseResult): ProcessResult {
        val success = parsedIngredient as ParseResult.ParseSuccess
        val normalized = normalizer.normalizePlForMatching(success.candidates.joinToString(" "))
        val norm = normalizeIngredient(normalized, ingredientNormalizationMap)
        val processResult = findMatch(norm)
        return processResult
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

    fun generatePhrases(tokens: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (size in tokens.size downTo 1) {
            for (i in 0..tokens.size - size) {
                result += tokens.subList(i, i + size).joinToString(" ")
            }
        }
        return result
    }

    fun findMatch(tokens: List<String>): ProcessResult {
        var bestScore = 0.0
        val phrases = generatePhrases(tokens).toSet().sortedByDescending { phrase -> phrase.split(" ").size }
        var bestMatch: ProcessResult =
            ProcessResult.ProcessError(ProcessErrorType.UNKNOWN)

        for (phrase in phrases) {
            val phaseCount = phrase.split(" ").size
            val coverage = phaseCount.toDouble() / tokens.size
            val minScore =
                when (tokens.size) {
                    1 -> 0.80
                    2 -> 0.65
                    else -> 0.5
                }
            val key = phrase to minScore
            val result: ProcessResult = trigramCache[key]?.also {
                cacheHits++
            } ?: scraperRepository.findByTrigram(phrase, minScore).also {
                cacheMisses++
                trigramCache[key] = it
            }
            when (result) {
                is ProcessResult.IngredientMatch -> {
                    val weightedScore = result.score * coverage

                    if (
                        bestMatch !is ProcessResult.IngredientMatch ||
                        weightedScore > bestScore
                    ) {
                        bestScore = weightedScore
                        bestMatch = result
                    }
                }

                is ProcessResult.ProcessError -> {
                    if (bestMatch !is ProcessResult.IngredientMatch) {
                        bestMatch = result
                    }
                }
            }
            if (bestScore > 0.99) break
        }
        return when {
            bestMatch !is ProcessResult.IngredientMatch -> bestMatch

            bestScore < minAcceptedWeightedScore ->
                ProcessResult.ProcessError(ProcessErrorType.LOW_SCORE)

            else -> bestMatch
        }
    }

}

sealed interface ProcessResult {
    data class IngredientMatch(
        val ingredientId: Long,
        val name: String,
        val score: Double,
    ) : ProcessResult

    data class ProcessError(
        val type: ProcessErrorType,
    ) : ProcessResult {
        enum class ProcessErrorType {
            UNKNOWN, NOT_FOUND, LOW_SCORE
        }
    }
}

sealed interface RecipeResult {
    val total: Long

    data class RecipeSuccess(
        override val total: Long,
        val ingredientsIds: List<Long>

    ) : RecipeResult

    data class RecipeError(
        override val total: Long,
        val errorsCount: Long,
        val type: RecipeErrorType,
    ) : RecipeResult {
        enum class RecipeErrorType {
            PARSE, PROCESS, RECIPE
        }
    }
}
