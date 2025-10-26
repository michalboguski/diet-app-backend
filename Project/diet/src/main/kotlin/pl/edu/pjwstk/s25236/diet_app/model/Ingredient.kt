package pl.edu.pjwstk.s25236.diet_app.model

class Ingredient(val id: Long = 0, val name: String, val nutrients: Map<Nutrient, Double>) {
}