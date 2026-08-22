package pl.edu.pjwstk.s25236.dietapp

import io.vavr.control.Either
import io.vavr.control.Option
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import pl.edu.pjwstk.s25236.dietapp.JooqUtils.Companion.call
import pl.edu.pjwstk.s25236.dietapp.JooqUtils.Companion.intoGroup
import pl.edu.pjwstk.s25236.dietapp.JooqUtils.Companion.intoGroups
import pl.edu.pjwstk.s25236.dietapp.common.Error
import pl.edu.pjwstk.s25236.dietapp.ingredient.Ingredient
import pl.edu.pjwstk.s25236.dietapp.ingredient.IngredientRepository
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.INGREDIENT

@Repository
class IngredientJooqRepository(
    context: DSLContext,
    mapper: ValueObjectMapper,
) : JooqRepository(context, mapper),
    IngredientRepository {
    override fun retrieve(ingredientId: Long): Either<Error, Option<Ingredient>> =
        call {
            context
                .select()
                .from(INGREDIENT)
                .where(INGREDIENT.ID.eq(ingredientId))
                .fetch()
        }.map { result ->
            intoGroup(results = result, table = INGREDIENT)
                .map { mapper.toIngredient(it) }
        }

    override fun retrieveAll(): Either<Error, List<Ingredient>> =
        call {
            context
                .select()
                .from(INGREDIENT)
                .fetch()
        }.map { result ->
            intoGroups(results = result, table = INGREDIENT)
                .map { mapper.toIngredient(it) }
        }
}
