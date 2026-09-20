package pl.edu.pjwstk.s25236.dietapp

import pl.edu.pjwstk.s25236.dietapp.bls.IngredientImporter
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeProcessor
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe
import pl.edu.pjwstk.s25236.dietapp.scrap.RecipeSource
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

fun main() {
//val i = IngredientImporter()
//    i.process()
//
    val procesor = RecipeProcessor()
    val list = mutableListOf<Double>()
    readAllRawRecipes(Path.of("saved-recipes"))[3]
        .let { recipe ->
        val rawRecipe =
            RawRecipe(
                source = RecipeSource.PRZESLIJ_PRZEPIS,
                link = "stgwg",
                title = " title",
                ingredientLines = recipe.ingredientLines,
                stepLines = recipe.stepLines,
            )
        val score = procesor.process(rawRecipe)
            println("Score: $score")
    }

}

    fun saveRawRecipe(
        baseDir: Path,
        recipe: RawRecipe,
    ) {
        Files.createDirectories(baseDir)

        val fileName = "${recipe.title}.txt"
        val filePath = baseDir.resolve(fileName)
        val separator = "==========="

        val content =
            buildString {
                recipe.ingredientLines.forEach { appendLine(it) }
                appendLine(separator)
                recipe.stepLines.forEach { appendLine(it) }
            }

        Files.writeString(
            filePath,
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
        )
    }

    fun readAllRawRecipes(dir: Path): List<RawRecipeFile> =
        Files
            .list(dir)
            .filter { it.toString().endsWith(".txt") }
            .sorted()
            .map(::readRawRecipe)
            .toList()

    data class RawRecipeFile(
        val ingredientLines: List<String>,
        val stepLines: List<String>,
    )

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