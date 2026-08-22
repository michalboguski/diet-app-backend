package pl.edu.pjwstk.s25236.dietapp

import io.vavr.control.Either
import io.vavr.control.Option
import org.jooq.DSLContext
import pl.edu.pjwstk.s25236.dietapp.JooqUtils.Companion.call
import pl.edu.pjwstk.s25236.dietapp.JooqUtils.Companion.intoGroups
import pl.edu.pjwstk.s25236.dietapp.common.Error
import pl.edu.pjwstk.s25236.dietapp.recipe.Recipe
import pl.edu.pjwstk.s25236.dietapp.recipe.RecipeRepository
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.INGREDIENT
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.RECIPE
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.RECIPE_INGREDIENT

class RecipeJooqRepository(
    context: DSLContext,
    mapper: ValueObjectMapper,
) : JooqRepository(context, mapper),
    RecipeRepository {
    override fun retrieve(nutrientId: Long): Either<Error, Option<Recipe>> {
        TODO("Not yet implemented")
    }

    override fun retrieveAll(): Either<Error, List<Recipe>> =
        call {
            context
                .select(
                    RECIPE.fields().toList() +
                        INGREDIENT.fields().toList(),
                ).from(RECIPE)
                .leftJoin(RECIPE_INGREDIENT)
                .on(RECIPE_INGREDIENT.RECIPE_ID.eq(RECIPE.ID))
                .leftJoin(INGREDIENT)
                .on(INGREDIENT.ID.eq(RECIPE_INGREDIENT.INGREDIENT_ID))
                .fetch()
        }.map { result ->
            intoGroups(results = result, table = RECIPE)
                .map { mapper.toRecipe(it) }
        }
}
