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
        options.context = "Translate the following food ingredient name literally.\n" +
            "Preserve the exact noun–adjective structure.\n" +
            "Do NOT generalize, simplify, or merge meanings.\n" +
            "If the phrase contains qualifiers (e.g. canned, with skin, white, yolk),\n" +
            "Whole can be skip.\n" +
            "they MUST be preserved in the translation.\n" +
            "Return only the translated phrase. Examples:\n" +
            "\"jajko\" -> \"whole egg\"\n" +
            "\"białko jaja\" -> \"egg white\"\n" +
            "\"żółtko jaja\" -> \"egg yolk\"\n" +
            "\"filet z kurczaka ze skórą\" -> \"chicken breast with skin\"\n" +
            "\"fasola puszkowana\" -> \"canned beans\""
        return deeplClient.translateText(text, "en", "pl", options).text
    }
}
