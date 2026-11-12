package pl.edu.pjwstk.s25236.dietapp.processing

import kotlin.collections.filter
import kotlin.collections.map
import kotlin.text.split

class RecipeCleaner {
    private val banned =
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
            "polewa",
            "dodatkowo",
            "ciasto",
            "biszkopt",
            )

    private val separateRegex = Regex("""(\d)([a-zA-ZąćęłńóśżźĄĆĘŁŃÓŚŻŹ])""")
    private val dotBetweenLettersRegex = Regex("""(?<=\p{L})\.(?=\p{L})""")
    private val backslashFractionRegex = Regex("""(?<=\d)\\(?=\d)""")

    fun splitNumberAndUnit(line: String): List<String> =
        line.replace(separateRegex, "$1 $2").split(" ")

    fun String.separateWordsAfterDot(): String =
        replace(dotBetweenLettersRegex, " ")

    fun String.normalizeFractionSlash(): String =
        replace(backslashFractionRegex, "/")

    private fun splitLines(lines: List<String>): List<String> =
        lines.flatMap { line ->
            when {
                '+' in line ->
                    line.split('+')
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                line.trim() in setOf(
                    "sól, pieprz",
                    "sól i pieprz",
                    "● sól, pieprz",
                ) -> listOf("sól", "pieprz")

                else -> listOf(line)
            }
        }

    fun String.mapLine(): String =
        when (this.trim()) {
            "1 kostka masła" -> "200 g masła"
            "cukier waniliowy" -> "30 g cukier waniliowy"
            "cukier wanilinowy" -> "30 g cukier waniliowy"
            "1 cukier waniliowy" -> "30 g cukier waniliowy"
            "-1 cukier waniliowy" -> "30 g cukier waniliowy"
            "2 cukry waniliowe" -> "60 g cukier waniliowy"
            "cukier" -> "10 g cukier"
            "sok z 0.5 cytryny" -> "60 g cytryna"
            "sok z połowy cytryny" -> "60 g cytryna"
            "sok z cytryny" -> "120 g cytryna"
            "sok z 1 cytryny" -> "120 g cytryna"
            "1 cytryna" -> "120 g cytryna"
            "olej do smażenia" -> "20 g olej rzepakowy"
            "olej" -> "20 g olej rzepakowy"
            "sól" -> "5 g sól"
            "● sól" -> "5 g sól"
            "● pieprz" -> "5 g pieprz"
            "sól morska" -> "5 g sól"
            "pieprz" -> "5 g pieprz"
            "pieprz czarny" -> "5 g pieprz"
            "pieprz czarny świeżo mielony" -> "5 g pieprz"
            "2 budynie śmietankowe" -> "60 g budyń"
            "2 budynie waniliowe" -> "60 g budyń"
            "cukier puder do posypania" -> "10 g cukier"
            "cukier puder" -> "10 g cukier"
            "mąka pszenna do posypania stolnicy" -> "mąka pszenna 20 g"
            "olej rzepakowy do smażenia" -> "olej rzepakowy 20 g"
            "wiórki kokosowe" -> "100 g wiórki kokosowe"
            "natka pietruszki" -> "10 g natka pietruszki"
            "majeranek" -> "5 g majeranek"
            "bułka tarta" -> "bułka tarta 50 g"
            "papier do pieczenia" -> ""
            "2 x" -> ""
            "-1 śmietan-fix" -> ""
            else -> this
        }

    fun cleanLines(rawLines: List<String>): List<String> =
        splitLines(rawLines)
            .asSequence()
            .map { cleanLine(it) }
            .filter { it.isNotBlank() }
            .map { splitNumberAndUnit(it).joinToString(" ") }
            .toList()

    fun cleanLine(rawLine: String): String {
        if (rawLine.contains(Regex("""^\s*\*"""))) return ""
        return rawLine
            .removePartName()
            .removeLeadingDash()
            .removeWhitespace()
            .deleteContentAfterDash()
            .removeAsterisk()
            .normalizeFractionSlash()
            .normalizeTypography()
            .replace("ml.", "ml")
            .separateWordsAfterDot()
            .replace("ok.", "")
            .normalizeCommas()
            .removeBracketContent()
            .removeEndChars()
            .lowercase()
            .mapLine()
            .takeUnless { it.isBlank() || it.shouldReject(banned) }
            .orEmpty()
    }
}

fun String.deleteContentAfterDash(): String {
    return this.split(" - ").first().trim()
}

fun String.removeAsterisk(): String {
    return this.replace(Regex("(?<=\\S)\\*(?=\\s|$)"), "")
}

fun String.removePartName(): String {
    if (":" in this) {
        val second = this.split(":")[1].trim()
        return if (second.isNotBlank())
            this.replace(":", "")
        else ""
    }
    return this
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
        .replace("pół", "0.5")
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
    return tokens.any { it in banned }
}
