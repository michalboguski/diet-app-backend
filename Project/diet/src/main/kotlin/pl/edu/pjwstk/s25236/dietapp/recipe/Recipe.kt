package pl.edu.pjwstk.s25236.dietapp.recipe

import pl.edu.pjwstk.s25236.dietapp.ingredient.Ingredient

class Recipe(
    val id: Long,
    val ingredients: Map<Ingredient, Double>,
    val steps: List<Step>,
)
