package pl.edu.pjwstk.s25236.dietapp.common

import io.vavr.control.Either

object Success {
    fun ok(): Either<Error, Success> = Either.right(this)
}
