package pl.edu.pjwstk.s25236.dietapp

import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult
import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult.ParseError.ParseErrorType
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeProcessor
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult.RecipeError
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult.RecipeSuccess
import pl.edu.pjwstk.s25236.dietapp.scrap.RecipeSource
import pl.edu.pjwstk.s25236.dietapp.utils.ScraperUtils
import kotlin.collections.map

fun main() {
//    IngredientImporter().processByNameEn("Coffee instant powder")

//      val browser = PlaywrightBrowser()
//      val przeslijPrzepisScraper = PrzeslijPrzepisScraper(browser)
//
//      val przeslijPrzepisLink = "https://www.przyslijprzepis.pl/przepisy"
//    przeslijPrzepisScraper.getLinksFromRange(przeslijPrzepisLink, 60, 8512) //8512
//    przeslijPrzepisScraper.processLinks()

    //testEveryLine()
    testRecipeProcesor()

     //testLines()
}

fun testLines() {
    val recipes = ScraperUtils.readAllRawRecipes(RecipeSource.PRZESLIJ_PRZEPIS)
    val lines = recipes.flatMap { it.ingredientLines }
    val xLines = lines.filter { "+" in it }
    println("all lines: ${lines.size}")
    println("lines witch xLines: ${xLines.size}")
    xLines.sortedBy { it.length }.forEach { println(it) }
}


fun testRecipeProcesor() {
    val rp = RecipeProcessor()
    var oneMiss = 0L
    var twoMiss = 0L
    var fullMatch = 0L
    var moreErrors = 0L
    println()
    val recipes = ScraperUtils.readAllRawRecipes(RecipeSource.PRZESLIJ_PRZEPIS)
    recipes.forEachIndexed { i, recipe ->
        val result = rp.process(recipe)
        when (result) {
            is RecipeSuccess -> {
                fullMatch++
            }

            is RecipeError -> {
                when (result.errorsCount) {
                    1L -> oneMiss++
                    2L -> twoMiss++
                    else -> {
                        moreErrors++
                    }
                }
            }
        }
    }
        .let {
            println()
            val s = recipes.size
            println("CACHE SIZE: ${rp.trigramCache.size}  HIT: ${rp.cacheHits}  Miss: ${rp.cacheMisses}")
            println("full: $fullMatch / $s  | ${fullMatch.toDouble() / s}")
            println("one err: $oneMiss / $s  | ${oneMiss.toDouble() / s}")
            println("two err: $twoMiss / $s  | ${twoMiss.toDouble() / s}")
            println("more err: $moreErrors / $s  | ${moreErrors.toDouble() / s}")

            println("unit err: ${rp.measurmentUnknownError}")
            println("amount err: ${rp.amountError}")
            println("NO candidates err: ${rp.noCandidatesError}")
            println("TO MANY candidates err: ${rp.toManyCandidatesError}")

            println("not found err: ${rp.notFoundError}")
            println("score err: ${rp.scoreError}")
            println()
            val cleanedByRecipe = recipes.map {
                rp.cleaner.cleanLines(it.ingredientLines)
            }
            println("raw lines: ${recipes.sumOf { it.ingredientLines.size }}")
            println("cleaned lines: ${cleanedByRecipe.sumOf { it.size }}")
            println("empty recipes: ${cleanedByRecipe.count { it.isEmpty() }}")

            println("unit ok: ${rp.unitOK}")
            println("amount ok: ${rp.amountOK}")
            println("candidates ok: ${rp.candidatesOK}")
            println("ingredient ok: ${rp.ingredientOK}")

            println("after parse: ${rp.afterParse}")
            println("after process: ${rp.afterProcess}")

            println()
            println("error unit map:")
            rp.unitErrorMap.entries
                .sortedByDescending { it.value }
                .take(30)
                .forEach {println("${it.value} ${it.key}")}

            println()
            println("error amount map:")
            rp.amountErrorMap.entries
                .sortedByDescending { it.value }
                .take(20)
                .forEach {println("${it.value} ${it.key}")}

            println()
            println("error no candidates map:")
            rp.noCandidatesErrorMap.entries
                .sortedByDescending { it.value }
                .take(10)
                .forEach {println("${it.value} ${it.key}")}

            println()
            println("error to many candidates map:")
            rp.toManyCandidatesErrorMap.entries
                .sortedByDescending { it.value }
                .take(10)
                .forEach {println("${it.value} ${it.key}")}

            println()
            println("Single parse error")
            rp.singleParseErrorPerRecipeList.forEach { println(it) }

            println()
            println("Single process error")
            rp.singleProcessErrorPerRecipeList.forEach { println(it) }
        }
}

fun testEveryLine() {
    val rp = RecipeProcessor()
    val cl = rp.cleaner
    val parser = rp.parser
    println()
    val recipes = ScraperUtils.readAllRawRecipes(RecipeSource.PRZESLIJ_PRZEPIS)
    val allRawLines = recipes.flatMap { it.ingredientLines }.toList()
    println("raw lines size: ${allRawLines.size}")
    val cleanedLines = allRawLines.map { rawLine ->
        cl.cleanLine(rawLine)
    }.filter { it.isNotEmpty() }
    println("cleaned lines size: ${cleanedLines.size}")

    val parsed = cleanedLines.map { parser.parseLine(it) }
    println("parsed ingredients size: ${parsed.size}")

    val unitErrorList = parsed.filterIsInstance<ParseResult.ParseError>().filter { it.type == ParseErrorType.UNIT }
    println("unit error size: ${unitErrorList.size}")
    val amountErrorList =
        parsed.filterIsInstance<ParseResult.ParseError>().filter { it.type == ParseErrorType.AMOUNT }
    println("amount error size: ${amountErrorList.size}")

    val filteredIngredients = parsed.filterIsInstance<ParseResult.ParseSuccess>()
    println("filtered ingredients size: ${filteredIngredients.size}")

    val processedIngredients = filteredIngredients.map { rp.processParsedIngredient(it) }
    println("processed ingredients size: ${processedIngredients.size}")

    val ingredientsErrorList = processedIngredients.filter { it == null }.toList()
    println("ingredients error size: ${ingredientsErrorList.size}")
    val ingredientsOkList = processedIngredients.filter { it != null }.toList()
    println("ingredients ok size: ${ingredientsOkList.size}")

}
