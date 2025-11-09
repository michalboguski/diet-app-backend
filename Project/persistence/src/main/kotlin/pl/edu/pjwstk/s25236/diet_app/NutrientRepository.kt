package pl.edu.pjwstk.s25236.diet_app

import io.vavr.control.Either
import io.vavr.control.Option
import pl.edu.pjwstk.s25236.diet_app.model.Error
import pl.edu.pjwstk.s25236.diet_app.model.Nutrient
import pl.edu.pjwstk.s25236.diet_app.model.Success

interface NutrientRepository {
    fun create(data: CreateNutrientData): Either<Error, Success>
    fun retrieve(nutrientId: Long) : Either<Error, Option<Nutrient>>

    data class CreateNutrientData(val name: String){}
}