package pl.edu.pjwstk.s25236.dietapp

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import pl.edu.pjwstk.s25236.dietapp.JooqUtils.Companion.RecordGroup
import pl.edu.pjwstk.s25236.dietapp.ingredient.Ingredient
import pl.edu.pjwstk.s25236.dietapp.nutrient.Nutrient
import pl.edu.pjwstk.s25236.dietapp.nutrient.NutrientRepository
import pl.edu.pjwstk.s25236.dietapp.recipe.Recipe
import pl.edu.pjwstk.s25236.dietapp.recipe.Step
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.records.IngredientRecord
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.records.NutrientRecord
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.records.RecipeRecord
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.INGREDIENT
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.NUTRIENT
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.RECIPE_INGREDIENT
import java.math.BigDecimal

@Component
class ValueObjectMapper(
    private val objectMapper: ObjectMapper,
) {
    private fun <T : Any> get(value: T?): T = requireNotNull(value) { "NULL in required field: check database" }

    fun toIngredient(group: RecordGroup<IngredientRecord>): Ingredient {
        val ingredientRecord = group.record
        return Ingredient(
            id = get(ingredientRecord.id),
            name = get(ingredientRecord.namePl),
        )
    }

    fun toRecipe(group: RecordGroup<RecipeRecord>): Recipe {
        val recipeRecord = group.record
        val ingredients: Map<Ingredient, BigDecimal> =
            group.references.filter { it[INGREDIENT.ID] != null }.associate { r ->
                Ingredient(
                    id = get(r[INGREDIENT.ID]),
                    name = get(r[INGREDIENT.NAME_PL]),
                ) to get(r[RECIPE_INGREDIENT.AMOUNT])
            }
        val steps: List<Step> =
            objectMapper.readValue(
                recipeRecord.steps?.data() ?: error("Recipe.steps is null"),
                object : TypeReference<List<Step>>() {},
            )
        return Recipe(
            id = get(recipeRecord.id),
            ingredients = ingredients,
            steps = steps,
        )
    }

    fun toNutrient(record: NutrientRecord): Nutrient =
        Nutrient(
            id = get(record.id),
            name = get(record.name),
            unit = get(record.unit),
        )

    fun toNutrientRecord(data: NutrientRepository.CreateNutrientData): NutrientRecord = NutrientRecord().with(NUTRIENT.NAME, data.name)
}
