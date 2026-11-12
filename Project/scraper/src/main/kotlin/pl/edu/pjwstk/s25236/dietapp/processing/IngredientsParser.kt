package pl.edu.pjwstk.s25236.dietapp.processing

import pl.edu.pjwstk.s25236.dietapp.common.Measurement
import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult.ParseError
import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult.ParseError.ParseErrorType

class IngredientLineParser {
    private val DECIMAL = """\d+(?:[.,]\d+)?"""
    private val FRACTION = """\d+\s*/\s*\d+"""
    private val NUMBER = """(?:$FRACTION|$DECIMAL)"""
    private val MIXED = """\d+\s+(?:i\s+)?$FRACTION"""
    private val amountRegex = Regex(
        """(?<![\p{L}\p{N}/.,])""" +
                """(?:$NUMBER\s*[-–—]\s*$NUMBER|$MIXED|$NUMBER\s+i\s+$NUMBER|$NUMBER)""" +
                """(?![\p{L}\p{N}/.,])"""
    )
    private val rangeSeparator = Regex("""\s*[-–—]\s*""")
    private val additionSeparator = Regex("""\s+i\s+""")
    private val mixedSeparator = Regex("""\s+(?:i\s+)?""")
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
            "zimnego",
            "startej",
            "startego",
        )

    fun parse(ingredientLines: List<String>): List<ParseResult> {
        return ingredientLines
            .map { parseLine(it) }
            .toList()
    }

    fun parseLine(line: String): ParseResult {
        val tokens = line.split(" ")
        val words = ignoreTokens(tokens)
        val unit = extractUnitToken(words)
        if (unit.first == Measurement.UNKNOWN) return ParseError(ParseErrorType.UNIT)
        val amount = extractAmount(words, unit.second)
        if (amount.first == -1.0) return ParseError(ParseErrorType.AMOUNT)
        val candidates =
            extractIngredientCandidates(
                words = words,
                indexesToDelete = amount.second.toSet() + unit.let {
                    if (it.first in Measurement.measurementsAsIngredients) return@let emptySet()
                    else return@let setOf(it.second)
                }
            )
        val candidatesSize = candidates.size
        return when {
            candidatesSize == 0 -> ParseError(ParseErrorType.NO_CANDIDATES)
            candidatesSize in (1..6) -> ParseResult.ParseSuccess(
                amount = amount.first,
                measurement = unit.first,
                candidates = candidates
            )

            candidatesSize > 6 -> ParseError(ParseErrorType.TO_MACH_CANDIDATES)
            else -> ParseError(ParseErrorType.UNKNOWN)
        }
    }

    fun extractAmount(words: List<String>, index: Int): Pair<Double, List<Int>> {
        if (index !in words.indices) return 0.0 to emptyList()
        if (index == 0) return 1.0 to emptyList()
        return parseMeasurementAtIndex(words, index)
    }

    fun extractUnitToken(words: List<String>): Pair<Measurement, Int> {
        words.forEachIndexed { index, word ->
            val m = Measurement.fromText(word)
            if (m != Measurement.UNKNOWN) return Pair(m, index)
        }
        return Pair(Measurement.UNKNOWN, -1)
    }

    fun extractIngredientCandidates(
        words: List<String>,
        indexesToDelete: Set<Int>,
    ): List<String> {
        return words.filterIndexed { index, _ -> index !in indexesToDelete }
    }

    fun ignoreTokens(tokens: List<String>): List<String> = tokens.filter { !ignoredTokens.contains(it) }.toList()

    private fun parseSimpleNumber(text: String): Double? {
        val parts = text.trim().split('/')

        if (parts.size == 1) {
            return parts[0].replace(',', '.').toDoubleOrNull()
                ?.takeIf { it.isFinite() }
        }

        if (parts.size != 2) return null

        val numerator = parts[0].trim().toDoubleOrNull() ?: return null
        val denominator = parts[1].trim().toDoubleOrNull() ?: return null

        if (denominator == 0.0) return null

        return (numerator / denominator).takeIf { it.isFinite() }
    }

    private fun parseAmount(text: String): Double? {
        val range = text.split(rangeSeparator)
        if (range.size == 2) {
            val from = parseSimpleNumber(range[0]) ?: return null
            val to = parseSimpleNumber(range[1]) ?: return null
            return from / 2.0 + to / 2.0
        }

        val addition = text.split(additionSeparator)
        if (addition.size == 2) {
            val first = parseSimpleNumber(addition[0]) ?: return null
            val second = parseSimpleNumber(addition[1]) ?: return null
            return first + second
        }

        val mixed = text.trim().split(mixedSeparator, limit = 2)
        if (mixed.size == 2 && mixed[0].all { it.isDigit() }) {
            val whole = parseSimpleNumber(mixed[0]) ?: return null
            val fraction = parseSimpleNumber(mixed[1]) ?: return null
            return whole + fraction
        }

        return parseSimpleNumber(text)
    }

    fun parseMeasurementAtIndex(
        words: List<String>,
        index: Int,
    ): Pair<Double, List<Int>> {
        val fallback = -1.0 to listOf(-1)
        val text = words.joinToString(" ")
        var offset = 0
        val wordRanges = words.map { word ->
            val range = offset until (offset + word.length)
            offset += word.length + 1
            range
        }
        val measurementRange = wordRanges[index]

        val match = amountRegex.findAll(text)
            .filter { match ->
                match.range.last < measurementRange.first ||
                        match.range.first > measurementRange.last
            }
            .minByOrNull { match ->
                if (match.range.last < measurementRange.first) {
                    measurementRange.first - match.range.last
                } else {
                    match.range.first - measurementRange.last
                }
            }
            ?: return fallback
        val amount = parseAmount(match.value) ?: return fallback
        val consumedIndices = wordRanges.indices.filter { wordIndex ->
            val range = wordRanges[wordIndex]
            !range.isEmpty() &&
                    range.first <= match.range.last &&
                    range.last >= match.range.first
        }
        return amount to consumedIndices
    }
}

sealed interface ParseResult {
    data class ParseSuccess(
        val amount: Double,
        val measurement: Measurement,
        val candidates: List<String>,
    ) : ParseResult

    data class ParseError(
        val type: ParseErrorType,
    ) : ParseResult {
        enum class ParseErrorType {
            UNIT, AMOUNT, NO_CANDIDATES, TO_MACH_CANDIDATES, UNKNOWN
        }
    }
}

