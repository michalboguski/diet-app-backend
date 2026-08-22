package pl.edu.pjwstk.s25236.dietapp

import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.control.Try
import org.jooq.Record
import org.jooq.Result
import org.jooq.Table
import pl.edu.pjwstk.s25236.dietapp.common.Error

class JooqUtils {
    companion object {
        data class RecordGroup<R : Record>(
            val record: R,
            val references: Result<out Record>,
        )

        fun <T> call(func: () -> T): Either<Error, T> =
            Try
                .ofSupplier { func() }
                .toEither()
                .mapLeft { throwable -> Error.Code.DATABASE_ERROR.toError() }

        fun <R1 : Record, R2 : Record> intoGroups(
            results: Result<R2>,
            table: Table<R1>,
        ): List<RecordGroup<R1>> =
            results
                .intoGroups(table)
                .entries
                .filter { (record, _) -> notEmpty(record) }
                .map { (record, references) ->
                    RecordGroup(record = record, references = references)
                }

        fun <R1 : Record, R2 : Record> intoGroup(
            results: Result<R2>,
            table: Table<R1>,
        ): Option<RecordGroup<R1>> =
            Option.of(
                intoGroups(results, table).firstOrNull(),
            )

        fun notEmpty(record: Record): Boolean {
            for (i: Int in 0..<record.size()) {
                if (record[i] != null) {
                    return true
                }
            }
            return false
        }
    }
}
