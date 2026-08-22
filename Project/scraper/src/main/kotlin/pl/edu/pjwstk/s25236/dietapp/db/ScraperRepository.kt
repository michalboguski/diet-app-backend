package pl.edu.pjwstk.s25236.dietapp.db

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import pl.edu.pjwstk.s25236.dietapp.bls.BlsIngredientDto
import pl.edu.pjwstk.s25236.dietapp.processing.IngredientMatch
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.INGREDIENT
import javax.sql.DataSource

class ScraperRepository(
    dataSource: DataSource,
) {
    private val context: DSLContext =
        DSL.using(dataSource, SQLDialect.POSTGRES)

    fun importFood(
        food: BlsIngredientDto,
        translate: String,
        norm: String,
    ) {
        context.transaction { configuration ->
            val transaction = DSL.using(configuration)
            transaction
                .insertInto(INGREDIENT)
                .set(INGREDIENT.NAME_DE, food.nameDe)
                .set(INGREDIENT.NAME_EN, food.nameEn)
                .set(INGREDIENT.NAME_PL, translate)
                .set(INGREDIENT.NORM, norm)
                .set(INGREDIENT.ENERGY_KJ, food.energyKj)
                .set(INGREDIENT.ENERGY_KCL, food.energyKcl)
                .set(INGREDIENT.WATER, food.water)
                .set(INGREDIENT.PROTEIN, food.protein)
                .set(INGREDIENT.FAT, food.fat)
                .set(INGREDIENT.CARBOHYDRATE_AVAILABLE, food.carbohydrateAvailable)
                .set(INGREDIENT.FIBRE_DIETARY_TOTAL, food.fibreDietaryTotal)
                .set(INGREDIENT.ALCOHOL, food.alcohol)
                .set(INGREDIENT.ORGANIC_ACIDS_TOTAL, food.organicAcidsTotal)
                .set(INGREDIENT.ASH, food.ash)
                .set(INGREDIENT.VITAMIN_A_RETINOL_EQUIVALENTS, food.vitaminARetinolEquivalents)
                .set(INGREDIENT.VITAMIN_A_RETINOL_ACTIVITY_EQUIVALENTS, food.vitaminARetinolActivityEquivalents)
                .set(INGREDIENT.RETINOL, food.retinol)
                .set(INGREDIENT.BETA_CAROTENE, food.betaCarotene)
                .set(INGREDIENT.CAROTENOIDS_EXCLUDING_BETA_CAROTENE, food.carotenoidsExcludingBetaCarotene)
                .set(INGREDIENT.VITAMIN_D, food.vitaminD)
                .set(INGREDIENT.VITAMIN_D3, food.vitaminD3)
                .set(INGREDIENT.VITAMIN_D2, food.vitaminD2)
                .set(INGREDIENT.VITAMIN_E, food.vitaminE)
                .set(INGREDIENT.ALPHA_TOCOPHEROL, food.alphaTocopherol)
                .set(INGREDIENT.BETA_TOCOPHEROL, food.betaTocopherol)
                .set(INGREDIENT.GAMMA_TOCOPHEROL, food.gammaTocopherol)
                .set(INGREDIENT.DELTA_TOCOPHEROL, food.deltaTocopherol)
                .set(INGREDIENT.ALPHA_TOCOTRIENOL, food.alphaTocotrienol)
                .set(INGREDIENT.VITAMIN_K, food.vitaminK)
                .set(INGREDIENT.VITAMIN_K1, food.vitaminK1)
                .set(INGREDIENT.VITAMIN_K2, food.vitaminK2)
                .set(INGREDIENT.VITAMIN_B1, food.vitaminB1)
                .set(INGREDIENT.VITAMIN_B2, food.vitaminB2)
                .set(INGREDIENT.NIACIN_EQUIVALENTS, food.niacinEquivalents)
                .set(INGREDIENT.NIACIN, food.niacin)
                .set(INGREDIENT.PANTOTHENIC_ACID, food.pantothenicAcid)
                .set(INGREDIENT.VITAMIN_B6, food.vitaminB6)
                .set(INGREDIENT.BIOTIN, food.biotin)
                .set(INGREDIENT.FOLATE_EQUIVALENT, food.folateEquivalent)
                .set(INGREDIENT.FOLATE, food.folate)
                .set(INGREDIENT.FOLIC_ACID_SYNTHETIC, food.folicAcidSynthetic)
                .set(INGREDIENT.VITAMIN_B12, food.vitaminB12)
                .set(INGREDIENT.VITAMIN_C, food.vitaminC)
                .set(INGREDIENT.SALT, food.salt)
                .set(INGREDIENT.SODIUM, food.sodium)
                .set(INGREDIENT.CHLORIDE, food.chloride)
                .set(INGREDIENT.POTASSIUM, food.potassium)
                .set(INGREDIENT.CALCIUM, food.calcium)
                .set(INGREDIENT.MAGNESIUM, food.magnesium)
                .set(INGREDIENT.PHOSPHORUS, food.phosphorus)
                .set(INGREDIENT.SULPHUR, food.sulphur)
                .set(INGREDIENT.IRON, food.iron)
                .set(INGREDIENT.ZINC, food.zinc)
                .set(INGREDIENT.IODIDE, food.iodide)
                .set(INGREDIENT.COPPER, food.copper)
                .set(INGREDIENT.MANGANESE, food.manganese)
                .set(INGREDIENT.FLUORIDE, food.fluoride)
                .set(INGREDIENT.CHROMIUM, food.chromium)
                .set(INGREDIENT.MOLYBDENUM, food.molybdenum)
                .set(INGREDIENT.ACETIC_ACID, food.aceticAcid)
                .set(INGREDIENT.CITRIC_ACID, food.citricAcid)
                .set(INGREDIENT.LACTIC_ACID, food.lacticAcid)
                .set(INGREDIENT.MALIC_ACID, food.malicAcid)
                .set(INGREDIENT.TARTARIC_ACID, food.tartaricAcid)
                .set(INGREDIENT.POLYOLS_TOTAL, food.polyolsTotal)
                .set(INGREDIENT.MANNITOL, food.mannitol)
                .set(INGREDIENT.SORBITOL, food.sorbitol)
                .set(INGREDIENT.XYLITOL, food.xylitol)
                .set(INGREDIENT.MONOSACCHARIDES_TOTAL, food.monosaccharidesTotal)
                .set(INGREDIENT.GLUCOSE, food.glucose)
                .set(INGREDIENT.FRUCTOSE, food.fructose)
                .set(INGREDIENT.GALACTOSE, food.galactose)
                .set(INGREDIENT.DISACCHARIDES_TOTAL, food.disaccharidesTotal)
                .set(INGREDIENT.SUCROSE, food.sucrose)
                .set(INGREDIENT.MALTOSE, food.maltose)
                .set(INGREDIENT.LACTOSE, food.lactose)
                .set(INGREDIENT.SUGARS_TOTAL, food.sugarsTotal)
                .set(INGREDIENT.OLIGOSACCHARIDES_AVAILABLE, food.oligosaccharidesAvailable)
                .set(INGREDIENT.STARCH, food.starch)
                .set(INGREDIENT.FIBRE_LOW_MOLECULAR_WEIGHT, food.fibreLowMolecularWeight)
                .set(INGREDIENT.FIBRE_HIGH_MOLECULAR_WEIGHT, food.fibreHighMolecularWeight)
                .set(INGREDIENT.FIBRE_WATER_INSOLUBLE, food.fibreWaterInsoluble)
                .set(INGREDIENT.FIBRE_WATER_SOLUBLE, food.fibreWaterSoluble)
                .set(INGREDIENT.FIBRE_HIGH_MOLECULAR_WEIGHT_WATER_SOLUBLE, food.fibreHighMolecularWeightWaterSoluble)
                .set(INGREDIENT.FIBRE_HIGH_MOLECULAR_WEIGHT_WATER_INSOLUBLE, food.fibreHighMolecularWeightWaterInsoluble)
                .set(INGREDIENT.FATTY_ACIDS_SATURATED_TOTAL, food.fattyAcidsSaturatedTotal)
                .set(INGREDIENT.FATTY_ACID_C4_0, food.fattyAcidC40)
                .set(INGREDIENT.FATTY_ACID_C6_0, food.fattyAcidC60)
                .set(INGREDIENT.FATTY_ACID_C8_0, food.fattyAcidC80)
                .set(INGREDIENT.FATTY_ACID_C10_0, food.fattyAcidC100)
                .set(INGREDIENT.FATTY_ACID_C12_0, food.fattyAcidC120)
                .set(INGREDIENT.FATTY_ACID_C14_0, food.fattyAcidC140)
                .set(INGREDIENT.FATTY_ACID_C15_0, food.fattyAcidC150)
                .set(INGREDIENT.FATTY_ACID_C16_0, food.fattyAcidC160)
                .set(INGREDIENT.FATTY_ACID_C17_0, food.fattyAcidC170)
                .set(INGREDIENT.FATTY_ACID_C18_0, food.fattyAcidC180)
                .set(INGREDIENT.FATTY_ACID_C20_0, food.fattyAcidC200)
                .set(INGREDIENT.FATTY_ACID_C22_0, food.fattyAcidC220)
                .set(INGREDIENT.FATTY_ACID_C24_0, food.fattyAcidC240)
                .set(INGREDIENT.FATTY_ACIDS_MONOUNSATURATED_TOTAL, food.fattyAcidsMonounsaturatedTotal)
                .set(INGREDIENT.FATTY_ACID_C14_1_N_5_CIS, food.fattyAcidC141N5Cis)
                .set(INGREDIENT.FATTY_ACID_C16_1_N_7_CIS, food.fattyAcidC161N7Cis)
                .set(INGREDIENT.FATTY_ACID_C18_1_N_7_CIS, food.fattyAcidC181N7Cis)
                .set(INGREDIENT.FATTY_ACID_C18_1_N_9_CIS, food.fattyAcidC181N9Cis)
                .set(INGREDIENT.FATTY_ACID_C20_1_N_9_CIS, food.fattyAcidC201N9Cis)
                .set(INGREDIENT.FATTY_ACID_C22_1_N_9_CIS, food.fattyAcidC221N9Cis)
                .set(INGREDIENT.FATTY_ACIDS_POLYUNSATURATED_TOTAL, food.fattyAcidsPolyunsaturatedTotal)
                .set(INGREDIENT.FATTY_ACIDS_POLYUNSATURATED_N_3_TOTAL, food.fattyAcidsPolyunsaturatedN3Total)
                .set(INGREDIENT.FATTY_ACID_C18_3_N_3_ALL_CIS, food.fattyAcidC183N3AllCis)
                .set(INGREDIENT.FATTY_ACID_C18_4_N_3_ALL_CIS, food.fattyAcidC184N3AllCis)
                .set(INGREDIENT.FATTY_ACID_C20_5_N_3_ALL_CIS, food.fattyAcidC205N3AllCis)
                .set(INGREDIENT.FATTY_ACID_C22_5_N_3_ALL_CIS, food.fattyAcidC225N3AllCis)
                .set(INGREDIENT.FATTY_ACID_C22_6_N_3_ALL_CIS, food.fattyAcidC226N3AllCis)
                .set(INGREDIENT.FATTY_ACIDS_POLYUNSATURATED_N_6_TOTAL, food.fattyAcidsPolyunsaturatedN6Total)
                .set(INGREDIENT.FATTY_ACID_C18_2_N_6_CISCIS, food.fattyAcidC182N6Ciscis)
                .set(INGREDIENT.FATTY_ACID_C18_2_CONJUGATED_CIS_9TRANS_11, food.fattyAcidC182ConjugatedCis9Trans11)
                .set(INGREDIENT.FATTY_ACID_C18_3_N_6_ALL_CIS, food.fattyAcidC183N6AllCis)
                .set(INGREDIENT.FATTY_ACID_C20_2_N_6_CISCIS, food.fattyAcidC202N6Ciscis)
                .set(INGREDIENT.FATTY_ACID_C20_3_N_6_ALL_CIS, food.fattyAcidC203N6AllCis)
                .set(INGREDIENT.FATTY_ACID_C20_4_N_6_ALL_CIS, food.fattyAcidC204N6AllCis)
                .set(INGREDIENT.FATTY_ACIDS_OTHERS, food.fattyAcidsOthers)
                .set(INGREDIENT.CHOLESTEROL, food.cholesterol)
                .set(INGREDIENT.AMINO_ACIDS_ESSENTIAL_TOTAL, food.aminoAcidsEssentialTotal)
                .set(INGREDIENT.ALANINE, food.alanine)
                .set(INGREDIENT.ARGININE, food.arginine)
                .set(INGREDIENT.ASPARTIC_ACID_INCLUDING_ASPARAGINE, food.asparticAcidIncludingAsparagine)
                .set(INGREDIENT.CYSTEINE, food.cysteine)
                .set(INGREDIENT.GLUTAMIC_ACID_INCLUDING_GLUTAMINE, food.glutamicAcidIncludingGlutamine)
                .set(INGREDIENT.GLYCINE, food.glycine)
                .set(INGREDIENT.HISTIDINE, food.histidine)
                .set(INGREDIENT.ISOLEUCINE, food.isoleucine)
                .set(INGREDIENT.LEUCINE, food.leucine)
                .set(INGREDIENT.LYSINE, food.lysine)
                .set(INGREDIENT.METHIONINE, food.methionine)
                .set(INGREDIENT.PHENYLALANINE, food.phenylalanine)
                .set(INGREDIENT.PROLINE, food.proline)
                .set(INGREDIENT.SERINE, food.serine)
                .set(INGREDIENT.THREONINE, food.threonine)
                .set(INGREDIENT.TRYPTOPHAN, food.tryptophan)
                .set(INGREDIENT.TYROSINE, food.tyrosine)
                .set(INGREDIENT.VALINE, food.valine)
                .set(INGREDIENT.NITROGEN_TOTAL, food.nitrogenTotal)
                .onConflictDoNothing()
                .execute()
        }

//    fun findByTrigram(
//        phrase: String,
//        minScore: Double,
//    ): List<IngredientMatch> {
//        val phraseParam = DSL.`val`(phrase)
//        val minScoreParam = DSL.`val`(minScore)
//
//        // ⬅️ WYRAŻENIE (bez aliasu) – do WHERE
//        val scoreExpr: Field<Double> =
//            DSL.field(
//                "diet_app.similarity({0}, cast({1} as text))",
//                Double::class.java,
//                INGREDIENT_PL.NAME,
//                phraseParam,
//            )
//
//        // ⬅️ ALIAS – tylko do SELECT / ORDER BY
//        val score = scoreExpr.`as`("score")
//
//        return context
//            .select(
//                INGREDIENT.ID,
//                INGREDIENT_PL.NAME,
//                score,
//            ).from(INGREDIENT_PL)
//            .join(INGREDIENT)
//            .on(INGREDIENT_PL.INGREDIENT_ID.eq(INGREDIENT.ID))
//            .where(
//                DSL.condition(
//                    "{0} % cast({1} as text)",
//                    INGREDIENT_PL.NAME,
//                    phraseParam,
//                ),
//            ).and(scoreExpr.ge(minScoreParam)) // ⬅️ TU JEST KLUCZOWA ZMIANA
//            .orderBy(score.desc())
//            .limit(3)
//            .fetch { r ->
//                IngredientMatch(
//                    ingredientId = r.get(INGREDIENT.ID)!!,
//                    name = r.get(INGREDIENT_PL.NAME)!!,
//                    score = r.get(score)!!,
//                )
//            }
//    }
    }

    fun findByTrigram(
        phrase: String,
        minScore: Double,
    ): List<IngredientMatch> {
        val phraseParam = DSL.`val`(phrase)
        val minScoreParam = DSL.`val`(minScore)

        // ⬅️ Pełne wyrażenie similarity (do WHERE)
        val scoreExpr: Field<Double> =
            DSL.field(
                "diet_app.similarity({0}, cast({1} as text))",
                Double::class.java,
                INGREDIENT.NORM,
                phraseParam,
            )

        // ⬅️ Alias tylko do SELECT / ORDER BY
        val score = scoreExpr.`as`("score")

        return context
            .select(
                INGREDIENT.ID,
                INGREDIENT.NAME_PL,
                score,
            ).from(INGREDIENT)
            .orderBy(score.desc())
            .limit(10)
            .fetch { r ->
                IngredientMatch(
                    ingredientId = r.get(INGREDIENT.ID)!!,
                    name = r.get(INGREDIENT.NAME_PL)!!,
                    score = r.get(score)!!,
                )
            }
    }

//    fun insertRecipe(recipe: RecipeDto): Long =
//        dsl.transactionResult { ctx ->
//            val tx = DSL.using(ctx)
//
//            val recipeId =
//                tx
//                    .insertInto(RECIPE)
//                    .set(RECIPE.NAME, recipe.name)
//                    .returning(RECIPE.ID)
//                    .fetchOne()
//                    ?.id
//                    ?: error("Failed to insert recipe")
//
//            recipe.ingredientIds.forEach { ingredientId ->
//                tx
//                    .insertInto(RECIPE_INGREDIENT)
//                    .set(RECIPE_INGREDIENT.RECIPE_ID, recipeId)
//                    .set(RECIPE_INGREDIENT.INGREDIENT_ID, ingredientId)
//                    .onConflictDoNothing()
//                    .execute()
//            }
//
//            recipeId
//        }
}
