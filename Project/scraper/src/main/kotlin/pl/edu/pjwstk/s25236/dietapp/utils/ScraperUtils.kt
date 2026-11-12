package pl.edu.pjwstk.s25236.dietapp.utils

import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe
import pl.edu.pjwstk.s25236.dietapp.scrap.RecipeSource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

object ScraperUtils {

//    fun printNormalizedIngredientNames() {
//        val recipesFromFiles = readAllRawRecipes(Path("saved-recipes"))
//        val cleaner = RecipeCleaner()
//        val parser = IngredientLineParser()
//        val normalizer = IngredientNameNormalizer()
//
//        recipesFromFiles.forEach { recipeFile ->
//            val cleanIngredientLines = cleaner.clean(recipeFile.ingredientLines)
//            val parsedIngredientLines = parser.parse(cleanIngredientLines)
//
//            parsedIngredientLines.forEachIndexed { i, data ->
//                val line = if (i < recipeFile.ingredientLines.size) recipeFile.ingredientLines[i] else "BRAK"
//                val candidates = data.candidates
//                val normalized = normalizer.normalizePlForMatching(candidates)
//                println("$line | $normalized")
//            }
//
//            println("=====")
//        }
//    }
//
//    fun printParsedIngredientNames() {
//        val recipesFromFiles = readAllRawRecipes(Path("saved-recipes"))
//        val cleaner = RecipeCleaner()
//        val parser = IngredientLineParser()
//        val normalizer = IngredientNameNormalizer()
//
//        recipesFromFiles.forEach { recipeFile ->
//            val cleanIngredientLines = cleaner.clean(recipeFile.ingredientLines)
//            val parsedIngredientLines = parser.parse(cleanIngredientLines)
//
//            parsedIngredientLines.forEachIndexed { i, data ->
//                val line = if (i < cleanIngredientLines.size) cleanIngredientLines[i] else "BRAK"
//                val candidates = data.candidates
//                println("$line | ${data.amount} ${data.measurement.names.first()} : $candidates")
//            }
//
//            println("=====")
//        }
//    }
//
//    fun printCleanedIngredientNames() {
//        val recipesFromFiles = readAllRawRecipes(Path("saved-recipes"))
//        val cleaner = RecipeCleaner()
//
//        recipesFromFiles.forEach { recipe ->
//            val cleanIngredientLines = cleaner.clean(recipe.ingredientLines)
//
//            cleanIngredientLines.forEachIndexed { idx, cleanLine ->
//                val rawLine =
//                    recipe.ingredientLines.getOrNull(idx)
//                        ?: "NO RAW LINE FOR INDEX $idx"
//
//                println("raw: $rawLine clean: $cleanLine")
//            }
//
//            println("=====")
//        }
//    }

    fun readAllRawRecipes(source: RecipeSource): List<RawRecipe> =
        Files.list(Path.of("data/${source.service}/recipes")).use { files ->
            files
                .filter { it.toString().endsWith(".txt") }
                .sorted()
                .map { file ->
                    val lines = Files.readAllLines(file)
                    val separatorIndex = lines.indexOf("===========")

                    require(separatorIndex >= 1) {
                        "Separator '===========' not found in file: $file"
                    }
                    val link = lines.first()
                    val ingredients = lines
                        .subList(1, separatorIndex)
                        .filter { it.isNotBlank() }

                    val steps = lines
                        .subList(separatorIndex + 1, lines.size)
                        .filter { it.isNotBlank() }

                    RawRecipe(
                        source = source,
                        link = link,
                        title = file.nameWithoutExtension,
                        ingredientLines = ingredients,
                        stepLines = steps,
                    )
                }
                .toList()
        }
}
