package pl.edu.pjwstk.s25236.dietapp.ingredient

import io.vavr.control.Either
import io.vavr.control.Option
import pl.edu.pjwstk.s25236.dietapp.common.Error

interface IngredientRepository {
    fun retrieve(ingredientId: Long): Either<Error, Option<Ingredient>>

    fun retrieveAll(): Either<Error, List<Ingredient>>
}
