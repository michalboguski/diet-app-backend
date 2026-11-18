package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.WaitForSelectorState

class DoradcaSmakuScraper(
    override val browser: PlaywrightBrowser,
) : RecipeScraper {
    override val source = RecipeSource.DORADCA_SMAKU
    override val paths = prepareServicePaths(source.service)
    override val page = browser.newPage(this)
    override fun getLinksFromPage(
        baseLink: String,
        pageNumber: Int
    ): List<String> {
        val url = buildPageUrl(baseLink, pageNumber)
        browser.openPage(page, url)
        val section = page.locator("#recipes_section")

        section.waitFor(
            Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(5_000.0)
        )
        val baseUri = java.net.URI.create(page.url())

        val links = page.locator("#recipes_section a[href]")

        return links
            .all()
            .mapNotNull { it.getAttribute("href") }
            .map { baseUri.resolve(it).toString() }
            .filter { href ->
                "przepis" in href && "node" !in href && "przepisy" !in href
            }
            .distinct()
    }

    override fun getRecipe(link: String): RawRecipe {
        browser.openPage(page, link)

        val ingredientContainer = page.locator(
            "#ingredients-element tr"
        )

        ingredientContainer.waitFor(
            Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(5_000.0)
        )

        val ingredientLines: List<String> = ingredientContainer.all()
            .map { item ->
                val cells = item.locator("td")

                val text = if (cells.count() > 0) {
                    cells.allTextContents().joinToString(" ") { it.trim() }
                } else {
                    item.innerText()
                }

                text.replace(Regex("\\s+"), " ").trim()
            }
            .filter { it.isNotBlank() }


        val stepsContainer = page.locator("article#recipe-element")


        stepsContainer.waitFor(
            Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(5_000.0)
        )
        val stepLines = stepsContainer.locator("p").all()
            .map { paragraph ->
                paragraph.innerText()
                    .replace(Regex("\\s+"), " ")
                    .trim()
            }
            .filter { it.isNotBlank() }

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
    ) = if (page == 1) base else "$base,$page"
}
