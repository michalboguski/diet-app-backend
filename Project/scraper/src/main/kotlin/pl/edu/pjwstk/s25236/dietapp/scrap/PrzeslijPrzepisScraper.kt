package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.WaitForSelectorState

class PrzeslijPrzepisScraper(
    override val browser: PlaywrightBrowser,
) : RecipeScraper {
    override val source = RecipeSource.PRZESLIJ_PRZEPIS
    override val paths = prepareServicePaths(source.service)
    override val page = browser.newPage(this)

    override fun buildPageUrl(
        base: String,
        page: Int,
    ) = if (page == 1) base else "$base?page=$page"

    override fun getRecipe(link: String): RawRecipe {
        browser.openPage(page, link)

        val ingredientsContainer = page.locator("#recpie-ingredient")
        ingredientsContainer.waitFor(
            Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(5_000.0)
        )

        val ingredientLines = ingredientsContainer
        .locator("ul li")
            .allInnerTexts()
            .map { it.replace('\u00A0', ' ').trim() }
            .filter { it.isNotBlank() }

        val stepsContainer = page.locator(
            ".row.preparationSteps"
        )

        val stepLines = stepsContainer.locator("ul li span:not(.position)")
            .allInnerTexts()
            .map { it.replace('\u00A0', ' ').trim() }
            .filter { it.isNotBlank() }

        return RawRecipe(
            source = source,
            link = page.url(),
            title = extractName(page.url()),
            ingredientLines = ingredientLines,
            stepLines = stepLines,
        )
    }

    override fun getLinksFromPage(baseLink: String, pageNumber: Int): List<String> {
        val url = buildPageUrl(baseLink, pageNumber)
        browser.openPage(page, url)
        return page.locator(
            ".col-12.recipe-item .row.mb20 .col-12.col-md-7 .box-item a"
        ).all()
            .mapNotNull { it.getAttribute("href") }
            .filter { href ->
                "przepis" in href && "uzytkownik" !in href
            }
            .distinct()
    }
}
