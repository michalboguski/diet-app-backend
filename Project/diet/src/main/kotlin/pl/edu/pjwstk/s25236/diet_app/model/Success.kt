package pl.edu.pjwstk.s25236.diet_app.model

import io.vavr.control.Either

object Success {
    fun ok(): Either<Error, Success> = Either.right(this)
}