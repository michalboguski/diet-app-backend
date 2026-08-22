package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Page
import com.microsoft.playwright.PlaywrightException
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe
import pl.edu.pjwstk.s25236.dietapp.scrap.RecipeSource

class PrzeslijPrzepisScraper(
    private val browser: PlaywrightBrowser,
) : RecipeScraper {
    override fun getBaseLink(): String = "https://www.przyslijprzepis.pl/przepisy"

    override fun buildPageUrl(
        base: String,
        page: Int,
    ) = if (page == 1) base else "$base?page=$page"

    override fun getRecipeLinksFromPage(page: Page): List<String> {
        clickRodo(page)
        val links: MutableList<String> = mutableListOf()
        val recipes = page.querySelectorAll(".col-12.recipe-item")
        recipes.forEach {
            val link: String = it.querySelector(".row.mb20 .col-12.col-md-7 .box-item a").getAttribute("href")
            links.add(link)
        }
        return links
    }

    // RawRecipe
    override fun getRecipe(recipePage: Page): RawRecipe {
        val ingredientsList: MutableList<String> = mutableListOf()
        val stepsList: MutableList<String> = mutableListOf()
        try {
            val ingredientString = recipePage.querySelector(".recpie-ingredient ul li").innerText()
            ingredientsList.addAll(ingredientString.split("\n").toMutableList())

            val stepsString = recipePage.querySelectorAll(".row.preparationSteps .col-12 ul li")
            stepsList.addAll(
                stepsString.mapNotNull { it.querySelector("span:not(.position)")?.innerText() }.toMutableList(),
            )
        } catch (e: PlaywrightException) {
            println("EXEPTION IN recipePage.querySelector()")
        }
        ingredientsList.removeIf { it.isBlank() || it.contains(":") }
        return RawRecipe(
            source = RecipeSource.PRZESLIJ_PRZEPIS,
            link = recipePage.url(),
            title = recipePage.title(),
            ingredientLines = ingredientsList,
            stepLines = stepsList,
        )
    }

    override fun scrap(pageNumber: Int): List<RawRecipe> {
        val recipeList: MutableList<RawRecipe> = mutableListOf()
        val page: Page = browser.newPage()
        blockAds(page)
        val link = buildPageUrl(getBaseLink(), pageNumber)
        println("link : $link")
        openPage(page, link)
        val recipesLinks = getRecipeLinksFromPage(page)
        for (recipeLink in recipesLinks) {
            println("recipeLink : $recipeLink")
            openPage(page, recipeLink)
            val recipeData = getRecipe(page)
            recipeList.add(recipeData)
        }
        return recipeList
    }
}
