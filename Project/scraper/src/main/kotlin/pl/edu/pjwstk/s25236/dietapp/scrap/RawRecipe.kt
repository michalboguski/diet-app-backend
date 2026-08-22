package pl.edu.pjwstk.s25236.dietapp.scrap

data class RawRecipe(
    val source: RecipeSource,
    val link: String,
    val title: String,
    val ingredientLines: List<String>,
    val stepLines: List<String>,
)
