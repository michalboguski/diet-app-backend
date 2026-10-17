package pl.edu.pjwstk.s25236.dietapp.processing

import pl.edu.pjwstk.s25236.dietapp.common.Measurement

class IngredientLineParser {
    val ignoredTokens =
        listOf(
            "i",
            "oraz",
            "lub",
            "duże",
            "małe",
            "świeże",
            "suszone",
            "mielone",
            "pokrojone",
            "opcjonalnie",
            "dowolne",
        )

    fun parse(ingredientLines: List<String>): List<ParsedIngredient> {
        return ingredientLines
            .map { splitNumberAndUnit(it) }
            .map { parseLine(it.split(" ")) }
            .toList()
    }

    fun parseLine(line: List<String>): ParsedIngredient {
        val words = ignoreTokens(line)
        val unit = extractUnitToken(words)
        val amount = extractAmount(words, unit.second)
        val candidates =
            extractIngredientCandidates(
                words = words,
                indexed = amount.second.toSet() + unit.let {
                    if (it.first in Measurement.measurementsAsIngredients) return@let -1
                    else return@let it.second
                }
            )
        println(words.joinToString(" ") + " | " + amount.second.toSet() + unit.second)
        return ParsedIngredient(
            amount = amount.first,
            measurement = unit.first,
            candidates = candidates,
        )
    }

    private val fractionPattern =
        Regex("""\d+\s*/\s*\d+""")

    private val regex =
        Regex("""\d+[.,]\d+|\d+""")

    private val separateRegex =
        Regex("""(\d)([a-zA-ZąćęłńóśżźĄĆĘŁŃÓŚŻŹ])""")

    fun splitNumberAndUnit(line: String): String = line.replace(separateRegex, "$1 $2")

    fun extractNumberTokens(line: String): List<String> =
        regex
            .findAll(line)
            .map { it.value }
            .toList()

    fun extractAmount(words: List<String>, index: Int): Pair<Double, List<Int>> {
        return when (index) {
            -1 -> 0.0 to listOf(-1)
            0 -> 1.0 to listOf(0)
            else -> parseMeasurementAtIndex(words, index)
        }
    }

    fun extractUnitToken(words: List<String>): Pair<Measurement, Int> {
        words.forEachIndexed { index, word ->
            val m = Measurement.fromText(word)
            if (m != Measurement.UNKNOWN) {
                return m to index
            }
        }

        return Measurement.UNKNOWN to -1
    }

    fun extractIngredientCandidates(
        words: List<String>,
        indexed: Set<Int>,
    ): String {
        return words.filterIndexed { index, _ -> index !in indexed }.joinToString(" ")
    }

    fun ignoreTokens(tokens: List<String>): List<String> = tokens.filter { !ignoredTokens.contains(it) }.toList()

    fun parseMeasurementAtIndex(
        words: List<String>,
        index: Int
    ): Pair<Double, List<Int>> {
        val amountMatches = words.filter { regex.matches(it) }
        return when (amountMatches.size) {
            0 -> 1.0 to listOf(-1)
            1 -> parseOneToken(words, index,words.indexOf(amountMatches[0]))
            2 -> parseTwoTokens(words, index, words.indexOf(amountMatches[0]), words.indexOf(amountMatches[1]))
            else -> 1.0 to listOf(-1) // to sprawdzić - zrobione na szybko
        }
    }

    fun parseOneToken(words: List<String>, mIndex: Int, index1: Int): Pair<Double, List<Int>> {
        return words[index1].toDouble() to listOf(index1)
    }
}
    fun parseTwoTokens(words: List<String>, mIndex: Int, index1: Int, index2: Int): Pair<Double, List<Int>> {
        return when (index2 - index1) {
            1 -> words[index1].toDouble() + words[index2].toDouble() to listOf(index1, index2)
            2 -> {
                val operator = words[index2-1].trim()
                when (operator) {
                    "-" -> words[index1].toDouble()*0.5 + words[index2].toDouble()*0.5 to listOf(index1, index2)
                    "i" -> words[index1].toDouble() + words[index2].toDouble() to listOf(index1, index2)
                    "/" -> words[index1].toDouble() / words[index2].toDouble() to listOf(index1, index2)
                    else -> words[index1].toDouble() to listOf(index1, index2)
                }
            }
            else -> words[index1].toDouble() to listOf(index1, index2)
        }
    }

    data class ParsedIngredient(
        val amount: Double,
        val measurement: Measurement,
        val candidates: String,
    )
