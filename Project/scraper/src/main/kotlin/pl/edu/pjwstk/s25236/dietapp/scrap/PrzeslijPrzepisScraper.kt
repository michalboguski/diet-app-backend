package pl.edu.pjwstk.s25236.dietapp.scrap

class PrzeslijPrzepisScraper(
    override val browser: PlaywrightBrowser,
) : RecipeScraper {
    override val service = "przyslijprzepis"
    override val paths = prepareServicePaths(service)
    override val page = browser.newPage(this)

    override fun buildPageUrl(
        base: String,
        page: Int,
    ) = if (page == 1) base else "$base?page=$page"

    override fun getRecipe(link: String): RawRecipe {
        openPage(page, link)

        val ingredients = page.locator(".recpie-ingredient ul li")
        val steps = page.locator(
            ".row.preparationSteps .col-12 ul li span:not(.position)"
        )
        val recipeTitle = extractName(page.url())

        ingredients.first().waitFor()
        steps.first().waitFor()

        val ingredientLines =
            ingredients.allInnerTexts().flatMap { it.lines() }.map { it.trim() }.filter { it.isNotBlank() }

        val stepLines = steps.allInnerTexts().map { it.trim() }.filter { it.isNotBlank() }

        return RawRecipe(
            source = RecipeSource.PRZESLIJ_PRZEPIS,
            link = page.url(),
            title = recipeTitle,
            ingredientLines = ingredientLines,
            stepLines = stepLines,
        )
    }

    override fun getLinksFromPage(baseLink: String, pageNumber: Int): List<String> {
        val url = buildPageUrl(baseLink, pageNumber)
        openPage(page, url)
        val links: MutableList<String> = mutableListOf()
        val recipes = page.querySelectorAll(".col-12.recipe-item")
        recipes.forEach {
            val link: String = it.querySelector(".row.mb20 .col-12.col-md-7 .box-item a").getAttribute("href")
            links.add(link)
        }
        return links
    }
}
