package pl.edu.pjwstk.s25236.dietapp

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitForSelectorState
import com.microsoft.playwright.options.WaitUntilState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult
import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult.ParseError.ParseErrorType
import pl.edu.pjwstk.s25236.dietapp.processing.ProcessResult
import pl.edu.pjwstk.s25236.dietapp.processing.ProcessResult.ProcessError.ProcessErrorType
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeCleaner
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeProcessor
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult.RecipeError
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult.RecipeError.RecipeErrorType
import pl.edu.pjwstk.s25236.dietapp.processing.RecipeResult.RecipeSuccess
import pl.edu.pjwstk.s25236.dietapp.scrap.AniaGotujeScraper
import pl.edu.pjwstk.s25236.dietapp.scrap.DoradcaSmakuScraper
import pl.edu.pjwstk.s25236.dietapp.scrap.KwestiaSmakuScraper
import pl.edu.pjwstk.s25236.dietapp.scrap.PlaywrightBrowser
import pl.edu.pjwstk.s25236.dietapp.scrap.PrzeslijPrzepisScraper
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe
import pl.edu.pjwstk.s25236.dietapp.scrap.RecipeSource
import pl.edu.pjwstk.s25236.dietapp.utils.ScraperUtils
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Files.list
import kotlin.collections.map
import kotlin.io.path.name

fun main() {

//    IngredientImporter().processByNameEn("Coffee instant powder")

    //  val browser = PlaywrightBrowser()

//    val przeslijPrzepisScraper = PrzeslijPrzepisScraper(browser)
//    val przeslijPrzepisLink = "https://www.przyslijprzepis.pl/przepisy"
//    przeslijPrzepisScraper.getLinksFromRange(przeslijPrzepisLink, 475, 8512) //8512

//     val aniaGotujeScraper = AniaGotujeScraper(browser)
//    val aniaGotujeLink = "https://aniagotuje.pl"
//     aniaGotujeScraper.getLinksFromRange(aniaGotujeLink, 1, 192)
    //  aniaGotujeScraper.processLinks()

    // val kwestiaSmakuScraper = KwestiaSmakuScraper(browser)
    // kwestiaSmakuScraper.processLinks()

//    val doradcaSmakuScraper = DoradcaSmakuScraper(browser)
//    val doradcaSmakuLink = "https://www.doradcasmaku.pl/przepisy"
//    doradcaSmakuScraper.getLinksFromRange(doradcaSmakuLink,1, 11209)


    // testEveryLine(RecipeSource.PRZESLIJ_PRZEPIS)
  //   testRecipeProcesor(RecipeSource.DORADCA_SMAKU)
 //   doradca()

    //   testLines("do smaku", RecipeSource.PRZESLIJ_PRZEPIS) // clean lines powinno być po cleanLine

    //  testEveryLineParse(RecipeSource.PRZESLIJ_PRZEPIS)
    //  testEveryLineProcess(RecipeSource.PRZESLIJ_PRZEPIS)

    // testCleanLineInRecipe(RecipeSource.ANIA_GOTUJE)

     runBlocking { scrapeRecipes() }
  //  runBlocking { scrapeLinks() }
}



suspend fun scrapeLinks(): Unit = coroutineScope {
    launch(Dispatchers.IO) {
        val browser = PlaywrightBrowser()
        browser.use { browser ->
            val scraper = PrzeslijPrzepisScraper(browser)
            scraper.getLinksFromRange("https://www.przyslijprzepis.pl/przepisy",511,8512)
        }
    }

//    launch(Dispatchers.IO) {
//        val browser = PlaywrightBrowser()
//        browser.use { browser ->
//            val scraper = DoradcaSmakuScraper(browser)
//            scraper.getLinksFromRange("https://www.doradcasmaku.pl/przepisy", 105, 11209)
//        }
//    }
}

suspend fun scrapeRecipes(): Unit = coroutineScope {


    launch(Dispatchers.IO) {
        val browser = PlaywrightBrowser()
        browser.use { browser ->
            val scraper = PrzeslijPrzepisScraper(browser)
            scraper.processLinks()
        }
    }
    launch(Dispatchers.IO) {
        val browser = PlaywrightBrowser()
        browser.use { browser ->
            val scraper = DoradcaSmakuScraper(browser)
            scraper.processLinks()
        }
    }

    launch(Dispatchers.IO) {
        val browser = PlaywrightBrowser()
        browser.use { browser ->
            val scraper = AniaGotujeScraper(browser)
            scraper.processLinks()
        }
    }

    launch(Dispatchers.IO) {
        val browser = PlaywrightBrowser()
        browser.use { browser ->
            val scraper = KwestiaSmakuScraper(browser)
            scraper.processLinks()
        }
    }
}

fun testCleanLineInRecipe(service: RecipeSource) {
    val cleaner = RecipeCleaner()
    val recipes = ScraperUtils.readAllRawRecipes(service)
    val recipeLinesMap = mutableMapOf<String, Int>()
    val differenceSizeMap = mutableMapOf<String, Pair<List<String>, List<String>>>()

    val rawLinesInRecipeCountMap = mutableMapOf<Int, Int>()
    val cleanLinesInRecipeCountMap = mutableMapOf<Int, Int>()
    recipes.forEach { recipe ->
        val rSize = recipe.ingredientLines.size
        recipeLinesMap[recipe.title] = rSize
        val cleanedRecipe = cleaner.cleanLines(recipe.ingredientLines)
        val clSize = cleanedRecipe.size

        if (rSize > 20) println("$rSize | ${recipe.title}")

        rawLinesInRecipeCountMap[rSize] = rawLinesInRecipeCountMap.getOrDefault(rSize, 0) + 1
        cleanLinesInRecipeCountMap[clSize] = cleanLinesInRecipeCountMap.getOrDefault(clSize, 0) + 1
        if (rSize != clSize) {
            differenceSizeMap[recipe.title] = recipe.ingredientLines to cleanedRecipe
        }
    }
    println()
    println("recipe line map size")
    recipeLinesMap.entries.sortedBy { it.value }.take(20).forEach { println("${it.value}: ${it.key}") }
    println()
    differenceSizeMap.entries.sortedByDescending { it.value.second.size - it.value.first.size }.take(10)
        .forEach { println("${it.value.second.size - it.value.first.size} | ${it.key}: ${it.value}") }

    println()
    println("raw lines counter: hits | times")
    rawLinesInRecipeCountMap.entries.sortedByDescending { it.key }.take(20)
        .forEach { println("${it.key}: ${it.value}") }
    println()
    println("clean lines counter: hits | times")
    cleanLinesInRecipeCountMap.entries.sortedByDescending { it.key }.take(20)
        .forEach { println("${it.key}: ${it.value}") }
}

fun testLines(str: String, service: RecipeSource) {
    val cleaner = RecipeCleaner()
    val recipesLines = ScraperUtils.readAllRawRecipes(service).flatMap { it.ingredientLines }
    val lines = cleaner.cleanLines(recipesLines)
    val xLines = recipesLines

        .filter { str in it }

    println("\nall lines: ${lines.size}")
    println("lines witch xLines: ${xLines.size}")
    xLines.sortedBy { it.length }.forEach { println(it) }
}

fun testRecipeProcesor(service: RecipeSource) {
    val rp = RecipeProcessor()
    var emptyRecipe = 0L

    var oneMiss = 0L
    var twoMiss = 0L
    var threeMiss = 0L
    var fullMiss = 0L
    var fullMatch = 0L
    var moreErrors = 0L

    var amountAndUnitError = 0L
    var measurmentUnknownError = 0L
    var amountError = 0L
    var noCandidatesError = 0L
    var toManyCandidatesError = 0L
    var notFoundError = 0L
    var scoreError = 0L
    var unknownFindError = 0L

    var unitOK = 0L
    var amountOK = 0L
    var candidatesOK = 0L
    var ingredientOK = 0L
    var ingredientNotOK = 0L

    var afterParseRecipeError = 0L
    var afterProcessRecipeError = 0L

    var afterParse = 0L
    var afterProcess = 0L

    val unitErrorMap = mutableMapOf<String, Int>()
    val amountErrorMap = mutableMapOf<String, Int>()
    val noCandidatesErrorMap = mutableMapOf<String, Int>()
    val toManyCandidatesErrorMap = mutableMapOf<String, Int>()

    val singleParseErrorPerRecipeList = mutableListOf<String>()
    val singleProcessErrorPerRecipeList = mutableListOf<String>()

    val singleParseErrorPerRecipeMap = mutableMapOf<String, Pair<Int, String>>()
    val singleProcessErrorPerRecipeMap = mutableMapOf<String, String>()

    println()

    val r1 = ScraperUtils.readAllRawRecipes(service)
    val r2 = ScraperUtils.readAllRawRecipes(RecipeSource.KWESTIA_SMAKU)
    val r3 = ScraperUtils.readAllRawRecipes(RecipeSource.ANIA_GOTUJE)
    val r4 = ScraperUtils.readAllRawRecipes(RecipeSource.PRZESLIJ_PRZEPIS)

    val recipes = r1 + r2 + r3 + r4
    recipes.forEach { recipe ->
        val cleanedLines = rp.cleaner.cleanLines(recipe.ingredientLines)
        if (cleanedLines.isEmpty() || cleanedLines.all { it.isEmpty() }) {
            emptyRecipe++;
            return@forEach
        }
        val total = cleanedLines.size.toLong()
        var errors = 0L
        var parsed = 0L
        var processed = 0L
        var errorLine: String = ""

        val parseResult = rp.parser.parse(cleanedLines)
            .onEachIndexed { i, parseResult ->
                when (parseResult) {
                    is ParseResult.ParseSuccess -> {
                        parsed++; candidatesOK++; unitOK++; amountOK++; }

                    is ParseResult.ParseError -> {
                        val key = cleanedLines[i]
                        errorLine = key
                        when (parseResult.type) {
                            ParseErrorType.UNIT -> {
                                measurmentUnknownError++
                                unitErrorMap[key] = unitErrorMap.getOrDefault(key, 0) + 1
                            }

                            ParseErrorType.AMOUNT -> {
                                unitOK++
                                amountError++
                                amountErrorMap[key] = amountErrorMap.getOrDefault(key, 0) + 1
                            }

                            ParseErrorType.NO_CANDIDATES -> {
                                unitOK++; amountOK++
                                noCandidatesError++
                                noCandidatesErrorMap[key] = noCandidatesErrorMap.getOrDefault(key, 0) + 1
                            }

                            ParseErrorType.TO_MACH_CANDIDATES -> {
                                unitOK++
                                amountOK++
                                toManyCandidatesError++
                                toManyCandidatesErrorMap[key] =
                                    toManyCandidatesErrorMap.getOrDefault(key, 0) + 1
                            }

                            ParseErrorType.UNKNOWN -> {
                                amountAndUnitError++
                            }

                        }
                        errors++
                    }
                }
            }
        if (errors == 1L) {
            singleParseErrorPerRecipeList.add(errorLine)
            singleParseErrorPerRecipeMap[recipe.title] = cleanedLines.size to errorLine
            errorLine = ""
        }
        if (errors > 0L || total != parsed) afterParseRecipeError++
        afterParse++

        val result: RecipeResult = when {
            errors > 0L || total != parsed -> {
                RecipeError(total, errors, RecipeErrorType.PARSE)
            }


            else -> {
                errors = 0L
                errorLine = ""

                val processResultList =
                    rp.processParsedIngredients(parseResult)

                processResultList.forEachIndexed { i, processResult ->
                    when (processResult) {
                        is ProcessResult.ProcessError -> {
                            errorLine = cleanedLines[i]

                            when (processResult.type) {
                                ProcessErrorType.LOW_SCORE -> scoreError++
                                ProcessErrorType.NOT_FOUND -> notFoundError++
                                ProcessErrorType.UNKNOWN -> unknownFindError++
                            }
                            ingredientNotOK++
                            errors++
                        }

                        is ProcessResult.IngredientMatch -> {
                            ingredientOK++
                            processed++
                        }
                    }
                }

                val processRecipeResult: RecipeResult = when {
                    errors > 0L -> {
                        RecipeError(total, errors, RecipeErrorType.PROCESS)
                    }

                    total != processed -> {
                        RecipeError(total, errors, RecipeErrorType.RECIPE)
                    }

                    else -> {
                        val ids = processResultList
                            .filterIsInstance<ProcessResult.IngredientMatch>()
                            .map { it.ingredientId }

                        RecipeSuccess(total, ids)
                    }
                }

                if (errors == 1L) {
                    singleProcessErrorPerRecipeList.add(errorLine)
                }

                if (errors > 0L) {
                    afterProcessRecipeError++
                }

                afterProcess++

                processRecipeResult
            }
        }
        when (result) {
            is RecipeSuccess -> fullMatch++
            is RecipeError -> {
                when (result.errorsCount) {
                    result.total -> fullMiss++
                    1L -> oneMiss++
                    2L -> twoMiss++
                    3L -> threeMiss++
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
            val cleanedByRecipe = recipes.map { rp.cleaner.cleanLines(it.ingredientLines) }
            println("raw lines: ${recipes.sumOf { it.ingredientLines.size }}")
            println("cleaned lines: ${cleanedByRecipe.sumOf { it.size }}")
            //   println("empty recipes after clean (clean all lines): ${cleanedByRecipe.count { it.isEmpty() }}")
            println("eroor unit and amount: $amountAndUnitError")
            println("unit err: $measurmentUnknownError")
            println("unit ok: $unitOK")
            println("amount err: $amountError")
            println("amount ok: $amountOK")
            println("NO candidates err: $noCandidatesError")
            println("TO MANY candidates err: $toManyCandidatesError")
            println("candidates ok: $candidatesOK")
            println("ingredient process ok: $ingredientOK")
            println("ingredient process NOT ok: $ingredientNotOK")

            println()
            println("CACHE SIZE: ${rp.trigramCache.size}  HIT: ${rp.cacheHits}  Miss: ${rp.cacheMisses}")
            println("unknown find in db error: $unknownFindError")
            println("not found err: $notFoundError}")
            println("score err: $scoreError}")

            println()
            println("full match: $fullMatch / $s  | ${fullMatch.toDouble() / s}")
            println("one err: $oneMiss / $s  | ${oneMiss.toDouble() / s}")
            println("two err: $twoMiss / $s  | ${twoMiss.toDouble() / s}")
            println("three err: $threeMiss / $s  | ${threeMiss.toDouble() / s}")
            println("more err: $moreErrors / $s  | ${moreErrors.toDouble() / s}")
            println("full error: $fullMiss / $s  | ${fullMiss.toDouble() / s}")
            println("after parse: ${afterParse}")
            println("after process: ${afterProcess}")

            println()
            println("error unit map:")
            unitErrorMap.entries
                .sortedByDescending { it.value }
                .take(10)
                .forEach { println("${it.value} ${it.key}") }

            println()
            println("error amount map:")
            amountErrorMap.entries
                .sortedByDescending { it.value }
                .take(10)
                .forEach { println("${it.value} ${it.key}") }

            println()
            println("error no candidates map:")
            noCandidatesErrorMap.entries
                .sortedByDescending { it.value }
                .take(10)
                .forEach { println("${it.value} ${it.key}") }

            println()
            println("error to many candidates map:")
            toManyCandidatesErrorMap.entries
                .sortedByDescending { it.value }
                .take(10)
                .forEach { println("${it.value} ${it.key}") }

            println()
            println("Single parse error tokens")
            val parseErrTokenMap = mutableMapOf<String, Int>()
            singleParseErrorPerRecipeList.forEach {
                val tokens = it.split(" ")
                tokens.forEach { token -> parseErrTokenMap[token] = parseErrTokenMap.getOrDefault(token, 0) + 1 }
            }
            parseErrTokenMap.entries.sortedByDescending { it.value }.take(20)
                .forEach { println("${it.value} ${it.key}") }

            println()
            println("Single parse error recipe -> line map")
            singleParseErrorPerRecipeMap.entries
                .sortedBy { it.value.first }
                .take(20)
                .forEach { println("${it.value} ${it.key}") }


            println()
            println("Single process error tokens")
            val processErrTokenMap = mutableMapOf<String, Int>()
            singleProcessErrorPerRecipeList.forEach {
                val tokens = it.split(" ")
                tokens.forEach { token ->
                    processErrTokenMap[token] = processErrTokenMap.getOrDefault(token, 0) + 1
                }
            }
            processErrTokenMap.entries.sortedByDescending { it.value }.take(20)
                .forEach { println("${it.value} ${it.key}") }
            println("Single process error lines with 'g' token")

            singleProcessErrorPerRecipeList.filter { "g" in it.split(" ") }.forEach { println(it) }
        }
}

fun testEveryLine(service: RecipeSource) {
    val rp = RecipeProcessor()
    val cl = rp.cleaner
    val parser = rp.parser

    val rawRecipes = ScraperUtils.readAllRawRecipes(service)
    val rawLinesList = rawRecipes.flatMap { it.ingredientLines }
    val cleanAllLinesList = cl.cleanLines(rawLinesList)
    val parsed = parser.parse(cleanAllLinesList)

    val parsedOkList = parsed.filterIsInstance<ParseResult.ParseSuccess>()
    val parsedErrorList = parsed.filterIsInstance<ParseResult.ParseError>()
    val parsedUnitErrorList = parsedErrorList.filter { it.type == ParseErrorType.UNIT }
    val parsedAmountErrorList = parsedErrorList.filter { it.type == ParseErrorType.AMOUNT }
    val parsedNoCandidatesErrorList = parsedErrorList.filter { it.type == ParseErrorType.NO_CANDIDATES }
    val parsedToManyCandidatesErrorList = parsedErrorList.filter { it.type == ParseErrorType.TO_MACH_CANDIDATES }
    val parsedUnknownErrorList = parsedErrorList.filter { it.type == ParseErrorType.UNKNOWN }

    // tylko ok parsed procesuje dalej
    val processed = parsedOkList.map { rp.processParsedIngredient(it) }
    val processOKList = processed.filterIsInstance<ProcessResult.IngredientMatch>()
    val processErrorList = processed.filterIsInstance<ProcessResult.ProcessError>()
    val processNoFoundErrorList = processErrorList.filter { it.type == ProcessErrorType.NOT_FOUND }
    val processScoreErrorList = processErrorList.filter { it.type == ProcessErrorType.LOW_SCORE }
    val processUnknownErrorList = processErrorList.filter { it.type == ProcessErrorType.UNKNOWN }

    println()
    println("parsed ${parsed.size} lines")
    println("parsed ${parsedOkList.size} ok")
    println("parsed ${parsedErrorList.size} error")
    println("parsed ${parsedUnitErrorList.size} unit error")
    println("parsed ${parsedAmountErrorList.size} amount error")
    println("parsed ${parsedNoCandidatesErrorList.size} no candidates error")
    println("parsed ${parsedToManyCandidatesErrorList.size} toMany error")
    println("parsed ${parsedUnknownErrorList.size} unknown error")
    println()
    println("process ${processed.size} lines")
    println("process ${processOKList.size} ok")
    println("process ${processErrorList.size} error")
    println("process ${processNoFoundErrorList.size} unknown error")
    println("process ${processScoreErrorList.size} score")
    println("process ${processUnknownErrorList.size} unknown error")

    println()
    println("raw, clean, parsed, processed : ${rawLinesList.size} | ${cleanAllLinesList.size} | ${parsedOkList.size} | ${processOKList.size}")

    println()
    println("licznik lini parse error")
    val mapUnitError = mutableMapOf<String, Int>()
    parsedUnitErrorList.forEach { mapUnitError[it.cleanLine] = mapUnitError.getOrDefault(it.cleanLine, 0) + 1 }
    mapUnitError.entries
        .sortedByDescending { it.value }
        .take(10)
        .forEach { entry -> println("${entry.value}: ${entry.key}") }
    println()


}

private fun String.normalizeWhitespace(): String =
    replace(Regex("[\\s\\u00A0]+"), " ").trim()

