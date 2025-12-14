package pl.edu.pjwstk.s25236.diet_app.nutrient

import io.vavr.control.Either
import io.vavr.control.Option
import pl.edu.pjwstk.s25236.diet_app.common.Error
import pl.edu.pjwstk.s25236.diet_app.common.Success

interface NutrientRepository {
    fun create(data: CreateNutrientData): Either<Error, Success>
    fun retrieve(nutrientId: Long): Either<Error, Option<Nutrient>>
    fun retrieveAll(): Either<Error, List<Nutrient>>

    data class CreateNutrientData(val name: String) {}
}