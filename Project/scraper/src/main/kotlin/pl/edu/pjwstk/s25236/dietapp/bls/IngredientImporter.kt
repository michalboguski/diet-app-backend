package pl.edu.pjwstk.s25236.dietapp.bls

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.edu.pjwstk.s25236.dietapp.db.ScraperDb
import pl.edu.pjwstk.s25236.dietapp.db.ScraperRepository
import pl.edu.pjwstk.s25236.dietapp.processing.IngredientNameNormalizer
import pl.edu.pjwstk.s25236.dietapp.processing.removeBracketContent
import java.math.BigDecimal

class IngredientImporter {
    val allowed = setOf("raw", "canned", "dried")
    val banned =
        setOf(
            "frozen",
            "boiled",
            "braised",
            "fried",
            "deep-fried",
            "grilled",
            "poached",
            "stewed",
            "cured",
            "boiling",
            "goulash",
            "cold",
            "cola",
            "mix",
            "with",
            "cooked",
            "danish",
            "mixed",
            "soup",
            "cake",
            "cocktail",
            "infusion",
            "substitute",
            "common",
            "pasta",
            "vegetarian",
            "venison",
            "vienna",
            "wine",
            "cocktail",
            "mix/fruit",
            "salad",
            "drink",
            "mix",
            "viennese",
            "beer",
            "chocolate",
            "zervelatwurst",
            "vegetables",
            "instant",
            "vegetable",
            "ice",
            "juice",
        )

    fun saveIngredients(path: String) {
        val dataSource = ScraperDb.dataSource
        val repo = ScraperRepository(dataSource)
        val translator = IngredientTranslator()
        val normalizer = IngredientNameNormalizer()
        val ingredients = prepare(path).sortedBy { it.nameEn.length }

        var c = 0

        ingredients.forEach { ingredient ->
            val phrase =
                ingredient.nameEn
                    .lowercase()
                    .removeBracketContent()
                    .split(Regex("[^a-z0-9]+"))
                    .filter { it.isNotBlank() }
                    .filter { it !in banned }
                    .joinToString(" ")

            if (phrase.isBlank()) {
                println("   +++++ POMINIĘTO $c: phrase: ${ingredient.nameEn}")
                return@forEach
            }

            val translate = translator.translateToPl(phrase)
            val norm = normalizer.normalizePlForMatching(translate).joinToString(" ")
            println("${++c}: phrase: $phrase | translate: $translate | norm: $norm")
            repo.importFood(ingredient, translate, norm)
            Thread.sleep(250)
        }
    }

    fun prepare(path: String): List<BlsIngredientDto> {
        val multiSet = mutableSetOf<BlsIngredientDto>()
        val uniqueSet = mutableSetOf<BlsIngredientDto>()
        val mapFour = mutableMapOf<String, MutableList<BlsIngredientDto>>()
        val mapThird = mutableMapOf<String, MutableList<BlsIngredientDto>>()
        val mapDouble = mutableMapOf<String, MutableList<BlsIngredientDto>>()
        val mapSingle = mutableMapOf<String, MutableList<BlsIngredientDto>>()

        val ingredients = readXlsx(path)
        println("start: ${ingredients.size}")
        val remaining = ingredients.filter { it.tokens().all { token -> token !in banned } }.toMutableSet()
        println("filter banned: ${remaining.size}")

        remaining.forEach { ingredient ->
            val tokens = ingredient.tokens()
            if (tokens.keyOf(4) != null) mapFour.getOrPut(tokens.keyOf(4)!!) { mutableListOf() }.add(ingredient)
        }
        processGroups(mapFour, remaining, multiSet, uniqueSet)

        remaining.forEach { ingredient ->
            val tokens = ingredient.tokens()
            if (tokens.keyOf(3) != null) mapThird.getOrPut(tokens.keyOf(3)!!) { mutableListOf() }.add(ingredient)
        }
        processGroups(mapThird, remaining, multiSet, uniqueSet)

        remaining.forEach { ingredient ->
            val tokens = ingredient.tokens()
            if (tokens.keyOf(2) != null) {
                mapDouble.getOrPut(tokens.keyOf(2)!!) { mutableListOf() }.add(ingredient)
            }
        }
        processGroups(mapDouble, remaining, multiSet, uniqueSet)

        remaining.forEach { ingredient ->
            val tokens = ingredient.tokens()
            if (tokens.keyOf(1) != null) mapSingle.getOrPut(tokens.keyOf(1)!!) { mutableListOf() }.add(ingredient)
        }
        processGroups(mapSingle, remaining, multiSet, uniqueSet)

        val result =
            (uniqueSet + multiSet)
                .filterNot { it.nameEn.contains(Regex(""".*(\([^()]+\)|"[^"]+").*""")) }
                .map { transformName(it) }
                .toList()
        return result
    }

    private fun processGroups(
        group: Map<String, List<BlsIngredientDto>>,
        remaining: MutableSet<BlsIngredientDto>,
        multi: MutableSet<BlsIngredientDto>,
        unique: MutableSet<BlsIngredientDto>,
    ) {
        group.values.forEach { list ->
            if (list.size > 1) {
                reduceGroup(list).forEach { ingredient -> multi.add(ingredient) }
            } else {
                list.let { unique.add(it.first()) }
            }
            remaining.removeIf { it in list }
        }
    }

    private fun reduceGroup(list: List<BlsIngredientDto>): List<BlsIngredientDto> {
        val group = list.filter { it.nameEn.contains("fat") }
        return when {
            group.isNotEmpty() -> {
                listOf(group.maxBy { it.nameEn })
            }

            else -> {
                list
            }
        }
    }

    private fun transformName(dto: BlsIngredientDto): BlsIngredientDto {
        val nameTokens = dto.tokens().toMutableList()

        val indexOfMin =
            nameTokens.indexOfFirst {
                it.matches(Regex("""^(min|\d+(?:[.,]\d+)?)\.?$"""))
            }
        if (indexOfMin != -1) nameTokens.subList(indexOfMin, nameTokens.size)
        nameTokens.filter { "raw" !in it }

        val newName = nameTokens.joinToString(" ").normalize()

        return dto.copy(nameEn = newName)
    }

    private fun String.normalize(): String =
        replace(Regex("\\braw\\b"), "")
            .replace(Regex("[,\\s]+$"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun List<String>.keyOf(n: Int): String? = if (size >= n) take(n).joinToString(" ") else null

    fun readXlsx(path: String): List<BlsIngredientDto> {
        val inputStream =
            Thread
                .currentThread()
                .contextClassLoader
                .getResourceAsStream(path)
                ?: error("bls.xlsx not found in resources")

        inputStream.use { fis ->
            val workbook = XSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)
            val list = mutableListOf<BlsIngredientDto>()
            for (rowIndex in 2..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue

                val dto =
                    BlsIngredientDto(
                        row.getCell(0).stringCellValue,
                        row.getCell(1).stringCellValue,
                        BigDecimal.valueOf(row.getCell(2).numericCellValue),
                        readOrZero(row.getCell(3)),
                        readOrZero(row.getCell(4)),
                        readOrZero(row.getCell(5)),
                        readOrZero(row.getCell(6)),
                        readOrZero(row.getCell(7)),
                        readOrZero(row.getCell(8)),
                        readOrZero(row.getCell(9)),
                        readOrZero(row.getCell(10)),
                        readOrZero(row.getCell(11)),
                        readOrZero(row.getCell(12)),
                        readOrZero(row.getCell(13)),
                        readOrZero(row.getCell(14)),
                        readOrZero(row.getCell(15)),
                        readOrZero(row.getCell(16)),
                        readOrZero(row.getCell(17)),
                        readOrZero(row.getCell(18)),
                        readOrZero(row.getCell(19)),
                        readOrZero(row.getCell(20)),
                        readOrZero(row.getCell(21)),
                        readOrZero(row.getCell(22)),
                        readOrZero(row.getCell(23)),
                        readOrZero(row.getCell(24)),
                        readOrZero(row.getCell(25)),
                        readOrZero(row.getCell(26)),
                        readOrZero(row.getCell(27)),
                        readOrZero(row.getCell(28)),
                        readOrZero(row.getCell(29)),
                        readOrZero(row.getCell(30)),
                        readOrZero(row.getCell(31)),
                        readOrZero(row.getCell(32)),
                        readOrZero(row.getCell(33)),
                        readOrZero(row.getCell(34)),
                        readOrZero(row.getCell(35)),
                        readOrZero(row.getCell(36)),
                        readOrZero(row.getCell(37)),
                        readOrZero(row.getCell(38)),
                        readOrZero(row.getCell(39)),
                        readOrZero(row.getCell(40)),
                        readOrZero(row.getCell(41)),
                        readOrZero(row.getCell(42)),
                        readOrZero(row.getCell(43)),
                        readOrZero(row.getCell(44)),
                        readOrZero(row.getCell(45)),
                        readOrZero(row.getCell(46)),
                        readOrZero(row.getCell(47)),
                        readOrZero(row.getCell(48)),
                        readOrZero(row.getCell(49)),
                        readOrZero(row.getCell(50)),
                        readOrZero(row.getCell(51)),
                        readOrZero(row.getCell(52)),
                        readOrZero(row.getCell(53)),
                        readOrZero(row.getCell(54)),
                        readOrZero(row.getCell(55)),
                        readOrZero(row.getCell(56)),
                        readOrZero(row.getCell(57)),
                        readOrZero(row.getCell(58)),
                        readOrZero(row.getCell(59)),
                        readOrZero(row.getCell(60)),
                        readOrZero(row.getCell(61)),
                        readOrZero(row.getCell(62)),
                        readOrZero(row.getCell(63)),
                        readOrZero(row.getCell(64)),
                        readOrZero(row.getCell(65)),
                        readOrZero(row.getCell(66)),
                        readOrZero(row.getCell(67)),
                        readOrZero(row.getCell(68)),
                        readOrZero(row.getCell(69)),
                        readOrZero(row.getCell(70)),
                        readOrZero(row.getCell(71)),
                        readOrZero(row.getCell(72)),
                        readOrZero(row.getCell(73)),
                        readOrZero(row.getCell(74)),
                        readOrZero(row.getCell(75)),
                        readOrZero(row.getCell(76)),
                        readOrZero(row.getCell(77)),
                        readOrZero(row.getCell(78)),
                        readOrZero(row.getCell(79)),
                        readOrZero(row.getCell(80)),
                        readOrZero(row.getCell(81)),
                        readOrZero(row.getCell(82)),
                        readOrZero(row.getCell(83)),
                        readOrZero(row.getCell(84)),
                        readOrZero(row.getCell(85)),
                        readOrZero(row.getCell(86)),
                        readOrZero(row.getCell(87)),
                        readOrZero(row.getCell(88)),
                        readOrZero(row.getCell(89)),
                        readOrZero(row.getCell(90)),
                        readOrZero(row.getCell(91)),
                        readOrZero(row.getCell(92)),
                        readOrZero(row.getCell(93)),
                        readOrZero(row.getCell(94)),
                        readOrZero(row.getCell(95)),
                        readOrZero(row.getCell(96)),
                        readOrZero(row.getCell(97)),
                        readOrZero(row.getCell(98)),
                        readOrZero(row.getCell(99)),
                        readOrZero(row.getCell(100)),
                        readOrZero(row.getCell(101)),
                        readOrZero(row.getCell(102)),
                        readOrZero(row.getCell(103)),
                        readOrZero(row.getCell(104)),
                        readOrZero(row.getCell(105)),
                        readOrZero(row.getCell(106)),
                        readOrZero(row.getCell(107)),
                        readOrZero(row.getCell(108)),
                        readOrZero(row.getCell(109)),
                        readOrZero(row.getCell(110)),
                        readOrZero(row.getCell(111)),
                        readOrZero(row.getCell(112)),
                        readOrZero(row.getCell(113)),
                        readOrZero(row.getCell(114)),
                        readOrZero(row.getCell(115)),
                        readOrZero(row.getCell(116)),
                        readOrZero(row.getCell(117)),
                        readOrZero(row.getCell(118)),
                        readOrZero(row.getCell(119)),
                        readOrZero(row.getCell(120)),
                        readOrZero(row.getCell(121)),
                        readOrZero(row.getCell(122)),
                        readOrZero(row.getCell(123)),
                        readOrZero(row.getCell(124)),
                        readOrZero(row.getCell(125)),
                        readOrZero(row.getCell(126)),
                        readOrZero(row.getCell(127)),
                        readOrZero(row.getCell(128)),
                        readOrZero(row.getCell(129)),
                        readOrZero(row.getCell(130)),
                        readOrZero(row.getCell(131)),
                        readOrZero(row.getCell(132)),
                        readOrZero(row.getCell(133)),
                        readOrZero(row.getCell(134)),
                        readOrZero(row.getCell(135)),
                        readOrZero(row.getCell(136)),
                        readOrZero(row.getCell(137)),
                        readOrZero(row.getCell(138)),
                        readOrZero(row.getCell(139)),
                    )
                list.add(dto)
            }
            return list
        }
    }

    fun readOrZero(cell: Cell?): BigDecimal {
        if (cell == null) return BigDecimal.ZERO

        return when (cell.cellType) {
            CellType.NUMERIC -> {
                BigDecimal.valueOf(cell.numericCellValue)
            }

            CellType.STRING -> {
                val raw =
                    cell.stringCellValue
                        .trim()
                        .lowercase()

                when {
                    raw.isEmpty() -> {
                        BigDecimal.ZERO
                    }

                    raw == "-" -> {
                        BigDecimal.ZERO
                    }

                    raw.startsWith("<") -> {
                        BigDecimal.ZERO
                    }

                    else -> {
                        raw
                            .replace(",", ".")
                            .toBigDecimalOrNull()
                            ?: BigDecimal.ZERO
                    }
                }
            }

            CellType.FORMULA -> {
                try {
                    BigDecimal.valueOf(cell.numericCellValue)
                } catch (_: Exception) {
                    BigDecimal.ZERO
                }
            }

            else -> {
                BigDecimal.ZERO
            }
        }
    }
}
