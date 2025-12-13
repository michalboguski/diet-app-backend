package pl.edu.pjwstk.s25236.diet_app.ingredient

import pl.edu.pjwstk.s25236.diet_app.nutrient.Nutrient

class Ingredient(val id: Long = 0, val name: String, val nutrients: Map<Nutrient, Double>) {
}