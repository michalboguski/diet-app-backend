package pl.edu.pjwstk.s25236.dietapp.recipe

import pl.edu.pjwstk.s25236.dietapp.ingredient.Ingredient
import java.math.BigDecimal

class Recipe(
    val id: Long,
    val ingredients: Map<Ingredient, BigDecimal>,
    val steps: List<Step>,
)
