package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.options.WaitUntilState
import pl.edu.pjwstk.s25236.dietapp.scrap.RawRecipe
import java.nio.file.Paths

interface RecipeScraper {
    fun scrap(pageNumber: Int): List<RawRecipe>

    fun getBaseLink(): String

    fun getRecipeLinksFromPage(page: Page): List<String>

    fun getRecipe(recipePage: Page): RawRecipe

    fun buildPageUrl(
        base: String,
        page: Int,
    ): String

    fun openPage(
        page: Page,
        link: String,
    ): Page {
        try {
            page.navigate(
                link,
                Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(20_000.0),
            )
            page.tryClosePopups()
        } catch (e: PlaywrightException) {
            println("Błąd podczas ładowania strony: ${e.message}")
        }

        return page
    }

    fun clickRodo(page: Page) {
        try {
            val acceptBtn =
                page.waitForSelector(
                    "#accept-btn",
                    Page.WaitForSelectorOptions().setTimeout(2_000.0),
                )

            acceptBtn?.click()

            page.context().storageState(
                BrowserContext
                    .StorageStateOptions()
                    .setPath(Paths.get("state.json")),
            )

            println("Zaakceptowano RODO i zapisano state.json")
        } catch (e: PlaywrightException) {
            println("Brak przycisku RODO — prawdopodobnie już zaakceptowane")
        }
    }

    fun blockAds(page: Page) {
        page.context().route("**/*") { route ->
            if (route.request().url().contains("doubleclick")) {
                route.abort()
            } else {
                route.resume()
            }
        }
    }
}

fun Page.tryClosePopups() {
    val selectors =
        listOf(
            "button:has-text('Zamknij')",
            "button[aria-label='close']",
            ".popup__close",
            ".overlay__close",
        )
    for (selector in selectors) {
        try {
            locator(selector).click(
                Locator.ClickOptions().setTimeout(1000.0),
            )
            println("Popup zamknięty: $selector")
            break
        } catch (_: Exception) {
            // brak elementu – próbujemy kolejny
        }
    }
}
