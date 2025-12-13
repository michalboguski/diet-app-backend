package pl.edu.pjwstk.s25236.diet_app.common

data class Error(
    val code: Code = Code.UNKNOWN_APPLICATION_ERROR,
    val message: String
) {

    public fun isInternal() : Boolean {
        return Code.Type.INTERNAL == code.type
    }

    enum class Code(val type: Type, private val messageTemplate: String) {

        UNKNOWN_APPLICATION_ERROR(Type.INTERNAL, "Nieznany błąd aplikacji"),
        SERVER_ERROR(Type.INTERNAL, "Błąd serwera podczas %s"),
        DATABASE_ERROR(Type.INTERNAL, "Błąd bazy danych"),
        NUTRIENT_NOT_FOUND(Type.NOT_FOUND, "Nie znaleziono witaminy");

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