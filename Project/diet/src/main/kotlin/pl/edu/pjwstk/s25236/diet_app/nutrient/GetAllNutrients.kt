package pl.edu.pjwstk.s25236.diet_app.nutrient

import io.vavr.control.Either
import pl.edu.pjwstk.s25236.diet_app.common.Error

class GetAllNutrients(
    private val nutrientRepository: NutrientRepository
) {
    fun execute(): Either<Error, List<Nutrient>> {
        return nutrientRepository.retrieveAll();
    }
}