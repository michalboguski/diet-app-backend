package pl.edu.pjwstk.s25236.diet_app

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import pl.edu.pjwstk.s25236.diet_app.model.Error
import pl.edu.pjwstk.s25236.diet_app.model.Error.Code
import pl.edu.pjwstk.s25236.diet_app.model.Error.Code.Type.AUTHORIZATION
import pl.edu.pjwstk.s25236.diet_app.model.Error.Code.Type.CONFLICT
import pl.edu.pjwstk.s25236.diet_app.model.Error.Code.Type.NOT_FOUND
import pl.edu.pjwstk.s25236.diet_app.model.Error.Code.Type.TIMEOUT
import pl.edu.pjwstk.s25236.diet_app.model.Error.Code.Type.VALIDATION


abstract class BaseApiController() {

    protected fun <T> onError(error: Error): T {
        if (error.isInternal()) {
            // TODO Logowanie błędu
        }
        throw toErrorException(error)
    }

    private fun toErrorException(error: Error): ResponseStatusException {
        return ResponseStatusException(toHttpStatus(error.code), error.message)
    }

    private companion object {
        private fun toHttpStatus(errorCode: Code): HttpStatus {
            return when (errorCode.type) {
                NOT_FOUND -> HttpStatus.NOT_FOUND
                CONFLICT -> HttpStatus.CONFLICT
                TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT
                AUTHORIZATION -> HttpStatus.UNAUTHORIZED
                VALIDATION -> HttpStatus.BAD_REQUEST
                else -> HttpStatus.INTERNAL_SERVER_ERROR
            }
        }
    }
}