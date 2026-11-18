package pl.edu.pjwstk.s25236.dietapp.processing

import pl.edu.pjwstk.s25236.dietapp.common.Measurement
import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult.ParseError
import pl.edu.pjwstk.s25236.dietapp.processing.ParseResult.ParseError.ParseErrorType

class IngredientLineParser {
    private val DECIMAL = """\d+(?:[.,]\d+)?"""
    private val FRACTION = """\d+\s*/\s*\d+"""
    private val MIXED = """\d+\s+(?:i\s+)?$FRACTION"""
    private val NUMBER = """(?:$MIXED|$FRACTION|$DECIMAL)"""

    private val amountRegex = Regex(
        """(?<![\p{L}\p{N}/.,])""" + """(?:$NUMBER\s*[-–—]\s*$NUMBER|$NUMBER\s+i\s+$NUMBER|$NUMBER)""" + """(?![\p{L}\p{N}/.,])"""
    )

    private val mixedNumberRegex = Regex("""^(\d+)\s+(?:i\s+)?(\d+\s*/\s*\d+)$""")
    private val rangeSeparator = Regex("""\s*[-–—]\s*""")
    private val additionSeparator = Regex("""\s+i\s+""")

    val ignoredTokens = listOf(
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
        return ingredientLines.map { parseLine(it) }.toList()
    }

    fun parseLine(line: String): ParseResult {
        val tokens = line.split(" ")
        val words = ignoreTokens(tokens)
        val amount = extractAmount(words)
        val candidates = extractIngredientCandidates(
            words = words, indexesToDelete = amount.consumedIndexes.toSet()
        )
        val candidatesSize = candidates.size
        return when {
            amount.amount <= 0.0 && amount.measurement == Measurement.UNKNOWN -> ParseError(
                line, ParseErrorType.UNKNOWN
            )

            amount.amount <= 0.0 -> ParseError(line, ParseErrorType.AMOUNT)
            amount.measurement == Measurement.UNKNOWN -> ParseError(line, ParseErrorType.UNIT)
            candidatesSize == 0 -> ParseError(line, ParseErrorType.NO_CANDIDATES)
            candidatesSize > 6 -> ParseError(line, ParseErrorType.TO_MACH_CANDIDATES)
            candidatesSize in (1..6) -> ParseResult.ParseSuccess(
                cleanLine = line, amount = amount.amount, measurement = amount.measurement, candidates = candidates
            )

            else -> ParseError(line, ParseErrorType.UNKNOWN)
        }
    }

    fun extractAmount(words: List<String>): Amount {
        val text = words.joinToString(" ")
        var offset = 0
        val wordRanges = words.map { word ->
            val range = offset until (offset + word.length)
            offset += word.length + 1
            range
        }

        val measurementMatch = words.asSequence().mapIndexed { index, word ->
                Measurement.fromText(word) to index
            }.firstOrNull { (measurement, _) ->
                measurement != Measurement.UNKNOWN
            }

        val measurement = measurementMatch?.first ?: Measurement.UNKNOWN
        val measurementIndex = measurementMatch?.second
        val measurementRange = measurementIndex?.let { wordRanges[it] }

        val matches = amountRegex.findAll(text).filter { match ->
                measurementRange == null || match.range.last < measurementRange.first || match.range.first > measurementRange.last
            }

        val amountMatch = if (measurementRange == null) {
            matches.firstOrNull()
        } else {
            matches.minByOrNull { match ->
                if (match.range.last < measurementRange.first) {
                    measurementRange.first - match.range.last
                } else {
                    match.range.first - measurementRange.last
                }
            }
        }
        val indexesToDelete = buildSet {
            if (measurementIndex != null && measurement !in Measurement.measurementsAsIngredients) {
                add(measurementIndex)
            }
        }

        if (amountMatch != null) {
            val consumedIndexes = wordRanges.indices.filter { index ->
                val range = wordRanges[index]

                !range.isEmpty() && range.first <= amountMatch.range.last && range.last >= amountMatch.range.first
            }

            return Amount(
                amount = parseAmount(amountMatch.value) ?: 0.0,
                measurement = measurement,
                consumedIndexes = indexesToDelete + consumedIndexes,
            )
        }

        val defaultAmount = if (measurement == Measurement.UNKNOWN) {
            null
        } else {
            measurement.value.getOrNull()
        }

        val resultMeasurement = when {
            defaultAmount == null -> measurement

            measurement == Measurement.MILI_LITRE || measurement == Measurement.LITRE -> Measurement.MILI_LITRE

            else -> Measurement.GRAMS
        }

        return Amount(
            amount = defaultAmount ?: 1.0,
            measurement = resultMeasurement,
            consumedIndexes = indexesToDelete.toSet(),
            isDefault = true,
        )
    }

    fun extractIngredientCandidates(
        words: List<String>,
        indexesToDelete: Set<Int>,
    ): List<String> {
        return words.filterIndexed { index, _ -> index !in indexesToDelete }.filter { it.length > 1 || it == "%" }
    }

    fun ignoreTokens(tokens: List<String>): List<String> = tokens.filterNot { ignoredTokens.contains(it) }.toList()

    private fun parseSimpleNumber(text: String): Double? {
        val parts = text.trim().split('/')

        if (parts.size == 1) {
            return parts[0].replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
        }

        if (parts.size != 2) return null

        val numerator = parts[0].trim().toDoubleOrNull() ?: return null
        val denominator = parts[1].trim().toDoubleOrNull() ?: return null

        if (denominator == 0.0) return null

        return (numerator / denominator).takeIf { it.isFinite() }
    }

    private fun parseSingleAmount(text: String): Double? {
        val normalized = text.trim()
        val mixed = mixedNumberRegex.matchEntire(normalized)

        if (mixed != null) {
            val whole = parseSimpleNumber(mixed.groupValues[1]) ?: return null
            val fraction = parseSimpleNumber(mixed.groupValues[2]) ?: return null

            return (whole + fraction).takeIf { it.isFinite() }
        }

        return parseSimpleNumber(normalized)
    }

    private fun parseAmount(text: String): Double? {
        val normalized = text.trim()

        val range = normalized.split(rangeSeparator)
        if (range.size == 2) {
            val from = parseSingleAmount(range[0]) ?: return null
            val to = parseSingleAmount(range[1]) ?: return null

            return (from / 2.0 + to / 2.0).takeIf { it.isFinite() }
        }

        val addition = normalized.split(additionSeparator)
        if (addition.size == 2) {
            val first = parseSingleAmount(addition[0]) ?: return null
            val second = parseSingleAmount(addition[1]) ?: return null

            return (first + second).takeIf { it.isFinite() }
        }

        return parseSingleAmount(normalized)
    }
}

sealed interface ParseResult {
    val cleanLine: String

    data class ParseSuccess(
        override val cleanLine: String,
        val amount: Double,
        val measurement: Measurement,
        val candidates: List<String>,
    ) : ParseResult

    data class ParseError(
        override val cleanLine: String,
        val type: ParseErrorType,
    ) : ParseResult {
        enum class ParseErrorType {
            UNIT, AMOUNT, NO_CANDIDATES, TO_MACH_CANDIDATES, UNKNOWN
        }
    }
}

data class Amount(
    val amount: Double,
    val measurement: Measurement,
    val consumedIndexes: Set<Int>,
    val isDefault: Boolean = false,
)
