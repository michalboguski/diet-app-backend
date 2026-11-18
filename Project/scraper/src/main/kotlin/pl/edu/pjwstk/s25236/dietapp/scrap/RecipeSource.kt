package pl.edu.pjwstk.s25236.dietapp.scrap

enum class RecipeSource(
    val service: String
) {
    KWESTIA_SMAKU("kwestiasmaku"),
    ANIA_GOTUJE("aniagotuje"),
    PRZESLIJ_PRZEPIS("przyslijprzepis"),
    DORADCA_SMAKU("doradcasmku"),
    SMAKER("smker"),
    LIDL("lidl"),
    PRZEPISY_PL("przepisypl"),
    MOJE_GOTOWANIE("mojegotowanie"),
    PO_PROSTU_PYCHA("poprostupycha"),
    MNIAM_MNIAM("mniammniam"),
    BESZAMEL("bezaml"),
}