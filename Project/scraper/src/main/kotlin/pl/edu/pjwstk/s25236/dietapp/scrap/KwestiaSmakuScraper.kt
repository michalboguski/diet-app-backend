package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.WaitForSelectorState

class KwestiaSmakuScraper(
    override val browser: PlaywrightBrowser,
) : RecipeScraper {
    override val source = RecipeSource.KWESTIA_SMAKU
    override val paths = prepareServicePaths(source.service)
    override val page = browser.newPage(this)
    override fun getLinksFromPage(
        baseLink: String, pageNumber: Int
    ): List<String> {
        val url = buildPageUrl(baseLink, pageNumber)
        browser.openPage(page, url)
        val section = page.locator("#block-system-main")

        section.first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(5_000.0)
        )
        val baseUri = java.net.URI.create(page.url())

        val links = page.locator("#block-system-main a[href]")

        return links.all().mapNotNull { it.getAttribute("href") }.map { baseUri.resolve(it).toString() }
            .filter { href ->
                "przepis-" in href && "node" !in href && "przepisy" !in href
            }.distinct()
    }

    override fun getRecipe(link: String): RawRecipe {
        browser.openPage(page, link)

        val ingredientContainer = page.locator(".field-name-field-skladniki")

        ingredientContainer.first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(5_000.0)
        )

        val items = ingredientContainer.locator("table tr, ul li")

        items.first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(5_000.0)
        )

        val ingredientLines: List<String> = items.all().map { item ->
            val cells = item.locator("td")

            val text = if (cells.count() > 0) {
                cells.allTextContents().joinToString(" ") { it.trim() }
            } else {
                item.innerText()
            }

            text.replace(Regex("\\s+"), " ").trim()
        }.filter { it.isNotBlank() }

        val stepContainer = page.locator(
            "#recipe-element, " + ".field-name-field-przygotowanie:not(body:has(#recipe-element) *)"
        )

        stepContainer.waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(5_000.0)
        )

        val steps = stepContainer.locator("ul li")

        steps.first().waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(5_000.0)
        )

        val stepLines: List<String> = steps.allInnerTexts().map {
            it.replace('\u00A0', ' ').replace(Regex("\\s+"), " ").trim()
        }.filter { it.isNotBlank() }

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
    ) = if (page == 1) base else "$base/home-przepisy?page=$page"
}
