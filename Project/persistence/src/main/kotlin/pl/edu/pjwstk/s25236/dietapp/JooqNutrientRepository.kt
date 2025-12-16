package pl.edu.pjwstk.s25236.dietapp

import io.vavr.control.Either
import io.vavr.control.Option
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import pl.edu.pjwstk.s25236.dietapp.JooqUtils.Companion.call
import pl.edu.pjwstk.s25236.dietapp.common.Error
import pl.edu.pjwstk.s25236.dietapp.common.Success
import pl.edu.pjwstk.s25236.dietapp.common.Success.ok
import pl.edu.pjwstk.s25236.dietapp.nutrient.Nutrient
import pl.edu.pjwstk.s25236.dietapp.nutrient.NutrientRepository
import pl.edu.pjwstk.s25236.dietapp.nutrient.NutrientRepository.CreateNutrientData
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.NUTRIENT

@Repository
class JooqNutrientRepository(
    context: DSLContext,
    mapper: ValueObjectMapper,
) : JooqRepository(context, mapper),
    NutrientRepository {
    override fun create(data: CreateNutrientData): Either<Error, Success> =
        call {
            context
                .insertInto(NUTRIENT)
                .set(mapper.toNutrientRecord(data))
                .execute()
        }.flatMap { ok() }

    override fun retrieve(nutrientId: Long): Either<Error, Option<Nutrient>> =
        call {
            context
                .selectFrom(NUTRIENT)
                .where(NUTRIENT.ID.eq(nutrientId.toShort()))
                .fetchOptional()
        }.map { optionalRecord ->
            Option
                .ofOptional(optionalRecord)
                .map { mapper.toNutrient(it) }
        }

    override fun retrieve(name: String): Either<Error, Option<Nutrient>> =
        call {
            context
                .selectFrom(NUTRIENT)
                .where(NUTRIENT.NAME.eq(name))
                .fetchOptional()
        }.map { optionalRecord ->
            Option
                .ofOptional(optionalRecord)
                .map { mapper.toNutrient(it) }
        }

    override fun retrieveAll(): Either<Error, List<Nutrient>> =
        call {
            context
                .selectFrom(NUTRIENT)
                .fetch()
                .map { mapper.toNutrient(it) }
        }
}
