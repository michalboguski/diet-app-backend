package pl.edu.pjwstk.s25236.dietapp.ingredient

import io.vavr.control.Either
import io.vavr.control.Option
import pl.edu.pjwstk.s25236.dietapp.common.Error
import pl.edu.pjwstk.s25236.dietapp.common.Success

interface IngredientService {
    fun retrieveAll(): Either<Error, List<Ingredient>>

    fun retrieve(ingredientId: Long): Either<Error, Option<Ingredient>>
}
