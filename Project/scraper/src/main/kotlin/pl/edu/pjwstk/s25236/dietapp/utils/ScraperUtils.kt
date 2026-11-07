package pl.edu.pjwstk.s25236.dietapp.utils

import pl.edu.pjwstk.s25236.dietapp.processing.IngredientLineParser
import pl.edu.pjwstk.s25236.dietapp.processing.IngredientNameNormalizer
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeCleaner
import java.nio.file.Files
import java.nio.file.Path
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
                println("$line | ${data.amount} ${data.measurement.names.first()} : $candidates")
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

    fun readAllRawRecipes(dir: Path): List<RawRecipeFile> =
        Files
            .list(dir)
            .filter { it.toString().endsWith(".txt") }
            .sorted()
            .map(::readRawRecipe)
            .toList()

    fun readRawRecipe(file: Path): RawRecipeFile {
        val lines = Files.readAllLines(file)

        val separatorIndex = lines.indexOf("===========")
        require(separatorIndex >= 0) {
            "Separator '$===========' not found in file: $file"
        }
        val ingredients =
            lines
                .subList(0, separatorIndex)
                .filter { it.isNotBlank() }
        val steps =
            lines
                .subList(separatorIndex + 1, lines.size)
                .filter { it.isNotBlank() }
        return RawRecipeFile(ingredients, steps)
    }

    data class RawRecipeFile(
        val ingredientLines: List<String>,
        val stepLines: List<String>,
    )

}
