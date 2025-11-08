package pl.edu.pjwstk.s25236.diet_app

import io.vavr.control.Either
import io.vavr.control.Try
import pl.edu.pjwstk.s25236.diet_app.model.Error

class JooqUtils {
    companion object {
        fun <T> call(func: () -> T): Either<Error, T> =
            Try.ofSupplier { func() }
                .toEither()
                .mapLeft { throwable -> Error.Code.DATABASE_ERROR.toError() }
    }
}
