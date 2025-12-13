package pl.edu.pjwstk.s25236.diet_app.recipe

import pl.edu.pjwstk.s25236.diet_app.ingredient.Ingredient
import pl.edu.pjwstk.s25236.diet_app.recipe.Step

class Recipe(
    val id: Long,
    val ingredients: Map<Ingredient, Double>,
    val steps: List<Step>
) {
}