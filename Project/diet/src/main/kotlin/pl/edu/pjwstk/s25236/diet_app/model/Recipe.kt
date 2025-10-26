package pl.edu.pjwstk.s25236.diet_app.pl.edu.pjwstk.s25236.diet_generator.model

import pl.edu.pjwstk.s25236.diet_app.model.Ingredient
import pl.edu.pjwstk.s25236.diet_app.model.Step

class Recipe(
    val id: Long,
    val ingredients: Map<Ingredient, Double>,
    val steps: List<Step>) {
}
