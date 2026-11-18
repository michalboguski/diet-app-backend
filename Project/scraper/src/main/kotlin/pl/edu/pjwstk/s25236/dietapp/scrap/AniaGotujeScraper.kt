package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.WaitForSelectorState


class AniaGotujeScraper(
    override val browser: PlaywrightBrowser,
) : RecipeScraper {
    override val source = RecipeSource.ANIA_GOTUJE
    override val paths = prepareServicePaths(source.service)
    override val page = browser.newPage(this)
    override fun getLinksFromPage(
        baseLink: String, pageNumber: Int
    ): List<String> {
        val url = buildPageUrl(baseLink, pageNumber)
        browser.openPage(page, url)
        val baseUri = java.net.URI.create(page.url())
        return page.locator("a[href^='/przepis/']:has(h2)").all().mapNotNull { it.getAttribute("href") }
            .map { baseUri.resolve(it).toString() }.distinct()
    }

    override fun getRecipe(link: String): RawRecipe {
        browser.openPage(page, link)

        val ingredientContainer = page.locator("#recipeIngredients")

        ingredientContainer.waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(5_000.0)
        )

        val ingredientLines = ingredientContainer.locator("[itemprop='recipeIngredient']").allInnerTexts()
            .map { it.replace('\u00A0', ' ').trim() }.filter { it.isNotBlank() }


        val steps = page.locator(
            "[itemprop='recipeInstructions']"
        )

        steps.first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(5_000.0)
        )

        val stepLines =
            steps.locator("p").allInnerTexts().map { it.replace('\u00A0', ' ').trim() }.filter { it.isNotBlank() }

        return RawRecipe(
            source = source,
            link = page.url(),
            title = extractName(page.url()),
            ingredientLines = ingredientLines,
            stepLines = stepLines,
        )
    }

    override fun buildPageUrl(
        base: String,
        page: Int,
    ) = if (page == 1) base else "$base/strona/$page"
}
