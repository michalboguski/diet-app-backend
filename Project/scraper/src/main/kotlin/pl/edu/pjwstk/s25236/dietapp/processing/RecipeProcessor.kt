package pl.edu.pjwstk.s25236.dietapp.processing

import pl.edu.pjwstk.s25236.dietapp.db.ScraperDb
import pl.edu.pjwstk.s25236.dietapp.db.ScraperRepository
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe

class RecipeProcessor {
    val cleaner = RecipeCleaner()
    val parser = IngredientLineParser()
    val normalizer = IngredientNameNormalizer()

    //   val recipeRepository = RecipeRepository()
    val scraperRepository = ScraperRepository(ScraperDb.dataSource)

    fun process(raw: RawRecipe): Double {
        println()
        var matchCount = 0
        val cleanedLines: List<String> = cleaner.clean(raw.ingredientLines)
        val parsedIngredients: List<ParsedIngredient> = parser.parse(cleanedLines)
        parsedIngredients.forEach {
            val norm = normalizer.normalizePlForMatching(it.candidates)
            val match = findMatch(norm)
            if (match != null) {
                matchCount++
            }
            println("norm: ${it.candidates} |  ${match?.name ?: "NIE ZNALEZIONO" } | score: ${match?.score}")
        }

        return matchCount.toDouble() / parsedIngredients.size
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

    fun generatePhrases(tokens: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (size in tokens.size downTo 1) {
            for (i in 0..tokens.size - size) {
                result += tokens.subList(i, i + size).joinToString(" ")
            }
        }
        return result
    }

    fun findMatch(tokens: List<String>): IngredientMatch? {
        val phrases = generatePhrases(tokens)
        var bestMatch: IngredientMatch? = null
        var bestScore: Double = 0.0
        for (phrase in phrases) {
            val minScore =
                when (phrase.split(" ").size) {
                    1 -> 0.85
                    2 -> 0.7
                    else -> 0.5
                }
            val matches = scraperRepository.findByTrigram(phrase, minScore)
            for (match in matches) {
                val weightedScore = match.score // * phrase.split(" ").size

                if (bestMatch == null || weightedScore > bestScore) {
                    bestScore = weightedScore
                    bestMatch = match
                }
            }
            if (bestScore > 3.5) break
        }
        //  println("best score: $bestScore")
        return bestMatch
    }
}

data class IngredientMatch(
    val ingredientId: Long,
    val name: String,
    val score: Double,
)
