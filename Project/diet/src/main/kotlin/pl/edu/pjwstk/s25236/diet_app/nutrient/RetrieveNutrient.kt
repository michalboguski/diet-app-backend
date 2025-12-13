package pl.edu.pjwstk.s25236.diet_app.nutrient

import io.vavr.control.Either
import io.vavr.control.Option
import pl.edu.pjwstk.s25236.diet_app.common.Error

class RetrieveNutrient(
    private val nutrientRepository: NutrientRepository
) {
    fun execute(nutrientId: Long): Either<Error, Option<Nutrient>> {
        return nutrientRepository.retrieve(nutrientId)
    }
}