package pl.edu.pjwstk.s25236.diet_app.service

import io.vavr.control.Either
import pl.edu.pjwstk.s25236.diet_app.model.Error
import pl.edu.pjwstk.s25236.diet_app.model.Nutrient

interface NutrientService {
    fun listAllNutrients(): Either<Error, List<Nutrient>>
}