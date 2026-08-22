package pl.edu.pjwstk.s25236.dietapp.utils

import kotlinx.coroutines.runBlocking
import pl.edu.pjwstk.s25236.dietapp.bls.ObjectMappers
import pl.edu.pjwstk.s25236.dietapp.processing.IngredientLineParser
import pl.edu.pjwstk.s25236.dietapp.processing.IngredientNameNormalizer
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeCleaner
import pl.edu.pjwstk.s25236.dietapp.readAllRawRecipes
import pl.edu.pjwstk.s25236.dietapp.saveRawRecipe
import pl.edu.pjwstk.s25236.dietapp.scrap.PlaywrightBrowser
import pl.edu.pjwstk.s25236.dietapp.scrap.PrzeslijPrzepisScraper
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe
import kotlin.io.path.Path

object ScraperUtils {
    fun printNormalizedIngredientNames() {
        val recipesFromFiles = readAllRawRecipes(Path("saved-recipes"))
        val cleaner = RecipeCleaner()
        val parser = IngredientLineParser()
        val normalizer = IngredientNameNormalizer()

        recipesFromFiles.forEach { recipeFile ->
            val cleanIngredientLines = cleaner.clean(recipeFile.ingredientLines)
            val parsedIngredientLines = parser.parse(cleanIngredientLines)

            parsedIngredientLines.forEachIndexed { i, data ->
                val line = if (i < recipeFile.ingredientLines.size) recipeFile.ingredientLines[i] else "BRAK"
                val candidates = data.candidates
                val normalized = normalizer.normalizePlForMatching(candidates)
                println("$line | $normalized")
            }

            println("=====")
        }
    }

    fun printParsedIngredientNames() {
        val recipesFromFiles = readAllRawRecipes(Path("saved-recipes"))
        val cleaner = RecipeCleaner()
        val parser = IngredientLineParser()
        val normalizer = IngredientNameNormalizer()

        recipesFromFiles.forEach { recipeFile ->
            val cleanIngredientLines = cleaner.clean(recipeFile.ingredientLines)
            val parsedIngredientLines = parser.parse(cleanIngredientLines)

            parsedIngredientLines.forEachIndexed { i, data ->
                val line = if (i < cleanIngredientLines.size) cleanIngredientLines[i] else "BRAK"
                val candidates = data.candidates
                println("$line | ${data.amount} ${data.unitName} : $candidates")
            }

            println("=====")
        }
    }

    fun printCleanedIngredientNames() {
        val recipesFromFiles = readAllRawRecipes(Path("saved-recipes"))
        val cleaner = RecipeCleaner()

        recipesFromFiles.forEach { recipe ->
            val cleanIngredientLines = cleaner.clean(recipe.ingredientLines)

            cleanIngredientLines.forEachIndexed { idx, cleanLine ->
                val rawLine =
                    recipe.ingredientLines.getOrNull(idx)
                        ?: "NO RAW LINE FOR INDEX $idx"

                println("raw: $rawLine clean: $cleanLine")
            }

            println("=====")
        }
    }

    fun scrapAndSaveRecipesInFiles(): Unit =
        runBlocking {
            val outputDir = Path("saved-recipes")
            PlaywrightBrowser().use { browser ->
                val scraper = PrzeslijPrzepisScraper(browser)
                val result: List<RawRecipe> = scraper.scrap(2)

                result.forEach { recipe ->
                    println("link: ${recipe.link}")
                    saveRawRecipe(outputDir, recipe)
                }
            }
        }
}
