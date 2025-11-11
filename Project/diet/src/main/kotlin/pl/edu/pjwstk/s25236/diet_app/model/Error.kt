package pl.edu.pjwstk.s25236.diet_app.model

import pl.edu.pjwstk.s25236.diet_app.model.Error.Code.Type.INTERNAL
import pl.edu.pjwstk.s25236.diet_app.model.Error.Code.UNKNOWN_APPLICATION_ERROR

data class Error(
    val code: Code = UNKNOWN_APPLICATION_ERROR,
    val message: String
) {

    public fun isInternal() : Boolean {
        return INTERNAL == code.type
    }

    enum class Code(val type: Type, private val messageTemplate: String) {

        UNKNOWN_APPLICATION_ERROR(INTERNAL, "Nieznany błąd aplikacji"),
        SERVER_ERROR(INTERNAL, "Błąd serwera podczas %s"),
        DATABASE_ERROR(INTERNAL, "Błąd bazy danych");

        enum class Type() {
            NOT_FOUND,
            INTERNAL,
            CONFLICT,
            TIMEOUT,
            AUTHORIZATION,
            VALIDATION,
        }

        fun toError(): Error {
            return Error(code = this, message = messageTemplate)
        }

        fun toError(vararg arguments: Any): Error {
            return Error(
                code = this,
                message = String.format(messageTemplate, *arguments)
            )
        }
    }
}
