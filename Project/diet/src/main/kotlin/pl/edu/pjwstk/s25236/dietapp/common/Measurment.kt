package pl.edu.pjwstk.s25236.dietapp.common

import io.vavr.API.None
import io.vavr.API.Some
import io.vavr.control.Option

enum class Measurement(
    val names: List<String>,
    val value: Option<Double>,
) {
    GRAMS(listOf("g", "gram", "gramów", "gramy"), Some(1.0)),
    MILI_GRAM(listOf("mg", "miligram", "miligramów", "miligramy"), Some(0.001)),
    MIKRO_GRAM(listOf("µg", "uq", "mikrogram", "mikrogramów", "mikrogramy"), Some(0.000001)),
    KJ(listOf("kj"), None()),
    KCL(listOf("kcl"), None()),
    SPOON(listOf("łyżka", "łyżki", "łyżek"), Some(15.0)),
    TEASPOON(listOf("łyżeczka", "łyżeczki", "łyżeczek"), Some(5.0)),
    GLASS(listOf("szklanka", "szklanek", "szklanki"), Some(200.0)),
    BUNCH(listOf("pęczek", "pęczki", "pęczków"), Some(50.0)),
    STEM(listOf("łodyga", "łodyg", "łodygi"), Some(30.0)),
    PIECE(listOf("kawałek", "kawałki", "kawałków"), Some(1.0)),
    SEED(listOf("ziarno", "ziarna", "ziaren", "ziarenko", "ziarnenka", "ziarenek"), Some(1.0)),
    LEAF(listOf("liść", "liści", "liście"), Some(5.0)),
    UNKNOWN(listOf("unknown"), Some(1.0)),
    ;

    fun toGrams(quantity: Double): Double = value.map { it * quantity }.getOrElse(quantity)

    companion object {
        fun fromText(text: String): Measurement {
            val lowerText = text.lowercase()
            return entries.firstOrNull { measurement ->
                measurement.names.any { lowerText.contains(it) }
            } ?: UNKNOWN
        }
    }
}
