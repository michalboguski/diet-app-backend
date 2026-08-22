package pl.edu.pjwstk.s25236.dietapp.processing

class RecipeCleaner {
    val banned =
        setOf(
            "dekoracja",
            "wody",
            "woda",
            "smaku",
            "opcjonalnie",
            "oprószenia",
            "formę",
            "składniki",
            "cm",
            "dodatkowy",
            "barwniki",
            "obtaczania",
            "*",
        )

    fun clean(rawLines: List<String>): List<String> =
        splitLinesOnPlus(rawLines)
            .asSequence()
            .map { it.removeLeadingDash() }
            .map { it.removeWhitespace() }
            .map { it.normalizeTypography() }
            .map { it.replace("ml.", "ml") }
            .map { it.normalizeCommas() }
            .map { it.removeBracketContent() }
            .map { it.removeEndChars() }
            .map { it.lowercase() }
            .filter { it.isNotBlank() }
            .filter { it.shouldReject(banned) }
            .toList()

    private fun splitLinesOnPlus(lines: List<String>): List<String> =
        lines.flatMap { line ->
            if ('+' in line) {
                line
                    .split('+')
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            } else {
                listOf(line)
            }
        }
}

fun String.removeLeadingDash(): String {
    val bulletRegex = Regex("""^\s*[•\-–]\s+""")
    return this.replace(bulletRegex, "").trim()
}

fun String.removeWhitespace(): String {
    val whitespaceRegex = Regex("\\s+")
    return this.replace(whitespaceRegex, " ").trim()
}

fun String.removeEndChars(): String = this.replace(Regex("[,.;:]$"), "").trim()

fun String.removeBracketContent(): String {
    val bracketRegex = Regex("\\([^0-9]*\\)")
    return this.replace(bracketRegex, "").trim()
}

fun String.normalizeCommas(): String {
    val commaRegex = Regex("(\\d),(\\d)")
    return this.replace(commaRegex, "$1.$2").trim()
}

fun String.normalizeTypography(): String =
    this
        .replace(Regex("(\\d)½"), "$1.5")
        .replace(Regex("(\\d)¼"), "$1.25")
        .replace(Regex("(\\d)¾"), "$1.75")
        .replace("1/2", "0.5")
        .replace("1/3", "0.34")
        .replace("1/4", "0.25")
        .replace("3/4", "0.75")
        .replace("½", "0.5")
        .replace("⅓", "0.34")
        .replace("¼", "0.25")
        .replace("¾", "0.75")
        .replace("–", "-")
        .replace("—", "-")
        .replace("„", "\"")
        .replace("”", "\"")
        .trim()

private val splitRegex = Regex("[^\\p{L}0-9]+")

fun String.shouldReject(banned: Set<String>): Boolean {
    val tokens =
        this
            .lowercase()
            .split(" ")
            .filter { it.isNotBlank() }
            .map { it.trim() }
    return !tokens.any { it in banned }
}
