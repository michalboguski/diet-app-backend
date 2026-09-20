package pl.edu.pjwstk.s25236.dietapp.bls

import com.deepl.api.DeepLClient
import com.deepl.api.Formality
import com.deepl.api.TextTranslationOptions

class IngredientTranslator {
    val deeplClient = DeepLClient("c0611b06-69f7-44e5-b022-93637d4b4005:fx")

    fun translateToPl(text: String): String {
        val options = TextTranslationOptions()
        options.modelType = "prefer_literal"
        options.formality = Formality.PreferLess
        options.setPreserveFormatting(true)
        options.context = """
        The input is an English FOOD INGREDIENT name from a nutritional database.

        Translate it into Polish using the culinary/food meaning only.

        Do not interpret words using non-food meanings.
        Preserve all meaningful qualifiers such as:
        canned, dried, fresh, frozen, smoked, skinless, with skin,
        low-fat, lactose-free, pasteurised, cooked, raw.

        Do not generalize or simplify the ingredient.

        Examples:
        "rocket" -> "rukola"
        "witch" -> "flądra"
        "egg white" -> "białko jaja"
        "egg yolk" -> "żółtko jaja"
        "chicken breast with skin" -> "pierś z kurczaka ze skórą"
        "canned beans" -> "fasola konserwowa"
        "whole milk" -> "mleko pełnotłuste"

        Return only the Polish ingredient name.
    """.trimIndent()
        return deeplClient.translateText(text, "en", "pl", options).text
    }
}
