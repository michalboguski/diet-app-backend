package pl.edu.pjwstk.s25236.dietapp.recipe

import io.vavr.control.Either
import io.vavr.control.Option
import pl.edu.pjwstk.s25236.dietapp.common.Error

interface RecipeRepository {
    fun retrieve(nutrientId: Long): Either<Error, Option<Recipe>>

    fun retrieveAll(): Either<Error, List<Recipe>>
}
