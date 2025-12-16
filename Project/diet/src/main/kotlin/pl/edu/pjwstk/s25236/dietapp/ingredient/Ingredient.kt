package pl.edu.pjwstk.s25236.dietapp.ingredient

import pl.edu.pjwstk.s25236.dietapp.nutrient.Nutrient

class Ingredient(
    val id: Long = 0,
    val name: String,
    val nutrients: Map<Nutrient, Double>,
)
