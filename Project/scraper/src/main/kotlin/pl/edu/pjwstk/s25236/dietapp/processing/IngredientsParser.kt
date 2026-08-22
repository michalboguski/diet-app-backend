package pl.edu.pjwstk.s25236.dietapp.processing

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
        val ingredients = mutableListOf<ParsedIngredient>()
        for (line in ingredientLines) {
            val amountToken = extractAmount(line)
            val unitToken = extractUnitToken(line, amountToken.second ?: 0)
            val candidates =
                extractIngredientCandidates(
                    line = line,
                    unitName = unitToken.first,
                    index = unitToken.second,
                )
            ingredients.add(
                ParsedIngredient(
                    amount = amountToken.first ?: 1.0,
                    unitName = unitToken.first,
                    candidates = candidates,
                ),
            )
        }
        return ingredients
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

    fun extractAmount(line: String): Pair<Double?, Int?> {
        val separated = splitNumberAndUnit(line)
        val tokens = extractNumberTokens(separated)
        return when (tokens.size) {
            0 -> 0.0 to 0
            1 -> tokens.first().toDouble() to line.lastIndexOf(tokens[0])
            2 -> parseTwoTokens(line, tokens)
            3 -> parseThreeTokens(line, tokens)
            4 -> parseFourTokens(line, tokens)
            else -> parseMultipleTokens(tokens)
        }
    }

    fun extractUnitToken(
        line: String,
        index: Int,
    ): Pair<String, Int> {
        var start = index + 1
        while (start < line.length && !line[start].isLetter()) start++
        var end = start
        while (end < line.length && line[end].isLetter()) end++
        return line.substring(start, end) to end
    }

    fun extractIngredientCandidates(
        line: String,
        unitName: String,
        index: Int,
    ): String {
        val cutLine = line.substring(index).trim()
        val tokens = cutLine.split(" ").filter { it.isNotBlank() }
        return when (tokens.size) {
            0 -> unitName
            1 -> tokens.joinToString(separator = " ")
            else -> ignoreTokens(tokens).joinToString(separator = " ")
        }
    }

    fun ignoreTokens(tokens: List<String>): List<String> = tokens.filter { !ignoredTokens.contains(it) }.toList()

    fun parseTwoTokens(
        line: String,
        tokens: List<String>,
    ): Pair<Double?, Int?> {
        val first = tokens[0]
        val second = tokens[1]
        return when {
            line.contains("-") -> {
                (first.toDouble() + second.toDouble()) / 2 to line.lastIndexOf(second)
            }

            fractionPattern.containsMatchIn(line) -> {
                first.toDouble() / second.toDouble() to line.lastIndexOf(second)
            }

            line.contains(" i ") && second.contains(".") -> {
                first.toDouble() + second.toDouble() to line.lastIndexOf(second)
            }

            else -> {
                first.toDouble() to line.lastIndexOf(first)
            }
        }
    }

    fun parseThreeTokens(
        line: String,
        tokens: List<String>,
    ): Pair<Double?, Int?> = tokens[0].toDouble() to 1

    fun parseFourTokens(
        line: String,
        tokens: List<String>,
    ): Pair<Double?, Int?> = 4444.4444 to 1

    fun parseMultipleTokens(tokens: List<String>): Pair<Double?, Int?> = tokens[0].toDouble() to 1
}

data class ParsedIngredient(
    val amount: Double,
    val unitName: String,
    val candidates: String,
)
