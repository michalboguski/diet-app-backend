package pl.edu.pjwstk.s25236.dietapp.common

import io.vavr.API.None
import io.vavr.API.Some
import io.vavr.control.Option

enum class Measurement(
    val names: List<String>,
    val value: Option<Double>
) {
    KILO_GRAM(listOf("kg", "kilogram", "kilogramów", "kilogramy"), Some(1000.0)),
    DEKA_GRAM(listOf("dg", "dekagram", "dekagramów", "dekagramy"), Some(10.0)),
    GRAMS(listOf("g", "gram", "gramów", "gramy"), Some(1.0)),
    MILI_GRAM(listOf("mg", "miligram", "miligramów", "miligramy"), Some(0.001)),
    MIKRO_GRAM(listOf("µg", "uq", "mikrogram", "mikrogramów", "mikrogramy"), Some(0.000001)),
    MILI_LITRE(listOf("ml", "mililiter", "mililitry", "mililitrów"), Some(1.0)),
    KJ(listOf("kj"), None()),
    KCL(listOf("kcl"), None()),
    SPOON(listOf("łyżka", "łyżki", "łyżek"), Some(15.0)),
    TEASPOON(listOf("łyżeczka", "łyżeczki", "łyżeczek"), Some(5.0)),
    GLASS(listOf("szklanka", "szklanek", "szklanki", "szkl", "szk"), Some(200.0)),
    BUNCH(listOf("pęczek", "pęczki", "pęczków"), Some(50.0)),
    STEM(listOf("łodyga", "łodyg", "łodygi"), Some(30.0)),
    PIECE(listOf("kawałek", "kawałki", "kawałków"), Some(1.0)),
    SEED(listOf("ziarno", "ziarna", "ziaren", "ziarenko", "ziarnenka", "ziarenek"), Some(1.0)),
    LEAF(listOf("liść", "liści", "liście"), Some(5.0)),
    EGG(listOf("jajko", "jajka", "jajek", "jaj"), Some(45.0)),
    JELLY(listOf("galaretka", "galaretki", "galaretek"), Some(10.0)),
    PUDDING(listOf("budyń", "budyniu", "budyni"), Some(10.0)),
    KISSEL(listOf("kisiel", "kisieli", "kiślów"), Some(10.0)),
    CAN(listOf("puszka", "puszki", "puszek", "puszkę"), Some(400.0)),
    PINCH(listOf("szczypta", "szczypty", "szczyptę"), Some(0.3)),
    BAR(listOf("tabliczka", "tabliczki"), Some(100.0)),
    UNKNOWN(listOf("unknown"), Some(1.0)),
    ;

    fun toGrams(quantity: Double): Double = value.map { it * quantity }.getOrElse(quantity)

    companion object {
        private val wordRegex = Regex("""\p{L}+""")
        fun fromText(text: String): Measurement {
            val words = wordRegex.findAll(text.lowercase())
                .map { it.value }

            return words.firstNotNullOfOrNull { word ->
                entries.firstOrNull { measurement ->
                    word in measurement.names
                }
            } ?: UNKNOWN
        }

        val measurementsAsIngredients = setOf(EGG, JELLY, PUDDING, KISSEL)
    }
}
