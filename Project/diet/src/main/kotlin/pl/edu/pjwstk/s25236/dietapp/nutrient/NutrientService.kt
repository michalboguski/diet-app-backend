package pl.edu.pjwstk.s25236.dietapp.nutrient

import io.vavr.control.Either
import io.vavr.control.Option
import pl.edu.pjwstk.s25236.dietapp.common.Error
import pl.edu.pjwstk.s25236.dietapp.common.Success

interface NutrientService {
    fun create(cmd: CreateNutrientCommand): Either<Error, Success>

    fun retrieve(nutrientId: Long): Either<Error, Option<Nutrient>>

    fun retrieve(nutrientName: String): Either<Error, Option<Nutrient>>

    fun retrieveAll(): Either<Error, List<Nutrient>>
}
