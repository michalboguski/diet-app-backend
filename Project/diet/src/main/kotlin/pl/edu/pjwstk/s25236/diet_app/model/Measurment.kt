package pl.edu.pjwstk.s25236.diet_app.model

enum class Measurement(val names: List<String>, val value: Double) {
    GRAMS(listOf("g", "gram", "gramów", "gramy"), 1.0),
    SPOON(listOf("łyżka", "łyżki", "łyżek"), 15.0),
    TEASPOON(listOf("łyżeczka", "łyżeczki", "łyżeczek"), 5.0),
    GLASS(listOf("szklanka", "szklanek", "szklanki"), 200.0),
    BUNCH(listOf("pęczek", "pęczki", "pęczków"), 50.0),
    STEM(listOf("łodyga", "łodyg", "łodygi"), 30.0),
    PIECE(listOf("kawałek", "kawałki", "kawałków"), 1.0),
    SEED(listOf("ziarno", "ziarna", "ziaren", "ziarenko", "ziarnenka", "ziarenek"), 1.0),
    LEAF(listOf("liść", "liści", "liście"), 5.0),
    UNKNOWN(listOf("unknown"), 1.0);

    fun toGrams(quantity: Double): Double {
        return value * quantity
    }

    companion object {
        fun fromText(text: String): Measurement {
            val lowerText = text.lowercase()
            return entries.firstOrNull { measurement ->
                measurement.names.any { lowerText.contains(it) }
            } ?: UNKNOWN
        }

    }
}