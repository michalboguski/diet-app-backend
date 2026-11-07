package pl.edu.pjwstk.s25236.dietapp

import pl.edu.pjwstk.s25236.dietapp.scrap.PlaywrightBrowser
import pl.edu.pjwstk.s25236.dietapp.scrap.PrzeslijPrzepisScraper

fun main() {
//    IngredientImporter().processByNameEn("Coffee instant powder")

//    val procesor = RecipeProcessor()
//    readAllRawRecipes(Path.of("saved-recipes"))[7]
//        .let { recipe ->
//        val rawRecipe =
//            RawRecipe(
//                source = RecipeSource.PRZESLIJ_PRZEPIS,
//                link = "stgwg",
//                title = " title",
//                ingredientLines = recipe.ingredientLines,
//                stepLines = recipe.stepLines,
//            )
//        val score = procesor.process(rawRecipe)
//            println("Score: $score")
//    }

      val browser = PlaywrightBrowser()
      val przeslijPrzepisLink = "https://www.przyslijprzepis.pl/przepisy"
//    PrzeslijPrzepisScraper(browser).getLinksFromRange(przeslijPrzepisLink, 60, 8512) //8512

    PrzeslijPrzepisScraper(browser).processLinks()
}