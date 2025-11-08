package pl.edu.pjwstk.s25236.diet_app.model

class Recipe(
    val id: Long,
    val ingredients: Map<Ingredient, Double>,
    val steps: List<Step>
) {
}
