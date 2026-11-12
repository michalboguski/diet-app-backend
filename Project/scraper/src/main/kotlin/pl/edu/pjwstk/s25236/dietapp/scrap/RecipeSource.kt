package pl.edu.pjwstk.s25236.dietapp.scrap

enum class RecipeSource(
    val service: String
) {
    KWESTIA_SMAKU("kwestiasmaku"),
    ANIA_GOTUJE("aniagotuje"),
    PRZESLIJ_PRZEPIS("przyslijprzepis")
}