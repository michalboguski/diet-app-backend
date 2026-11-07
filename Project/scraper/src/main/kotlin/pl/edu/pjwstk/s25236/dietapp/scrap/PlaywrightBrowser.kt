package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.options.ServiceWorkerPolicy
import java.net.URI
import java.nio.file.Files

class PlaywrightBrowser : AutoCloseable {
    private val playwright = Playwright.create()
    private val browser = playwright.firefox().launch(BrowserType.LaunchOptions().setHeadless(true))

    fun newPage(recipeScraper: RecipeScraper): Page {
        val statePath = recipeScraper.paths.state

        val options = Browser.NewContextOptions().setServiceWorkers(ServiceWorkerPolicy.BLOCK)

        if (Files.exists(statePath)) {
            options.setStorageStatePath(statePath)
        }

        val context = browser.newContext(options)
        context.blockAds()
        return context.newPage()
    }

    fun closePage(recipeScraper: RecipeScraper, page: Page) {
        val context = page.context()


        context.use { context ->
            context.storageState(
                BrowserContext.StorageStateOptions().setPath(recipeScraper.paths.state)
            )
        }
    }

    override fun close() {
        playwright.use { _ ->
            browser.close()
        }
    }
}

fun BrowserContext.blockAds() {
    val blockedDomains = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "adnxs.com",
        "adsrvr.org",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "pubmatic.com",
        "rubiconproject.com",
        "openx.net",
        "smartadserver.com",
        "casalemedia.com",
        "indexww.com",
        "yieldmo.com",
        "sharethrough.com",
        "bidswitch.net",
        "amazon-adsystem.com",
        "scorecardresearch.com",
        "quantserve.com",
        "moatads.com",
        "adsafeprotected.com",
    )

    val blockedPaths = listOf(
        Regex("/pagead/", RegexOption.IGNORE_CASE),
        Regex("/adsystem/", RegexOption.IGNORE_CASE),
        Regex("/prebid([./])", RegexOption.IGNORE_CASE),
        Regex("/adsbygoogle", RegexOption.IGNORE_CASE),
    )

    route("**/*") { route ->

        val request = route.request()
        val url = request.url()

        val host = runCatching {
            URI(url).host?.lowercase().orEmpty()
        }.getOrDefault("")

        val blockedDomain = blockedDomains.any { domain ->
            host == domain || host.endsWith(".$domain")
        }

        val blockedPath = blockedPaths.any {
            it.containsMatchIn(url)
        }

        if (blockedDomain || blockedPath) {
            route.abort()
        } else {
            route.fallback()
        }
    }
}

fun Page.tryClosePopups() {
    val selectors = listOf(
        "button:has-text('Zamknij')",
        "button[aria-label='close']",
        ".popup__close",
        ".overlay__close",
    )

    for (selector in selectors) {
        val popup = locator(selector).first()

        if (popup.isVisible) {
            try {
                popup.click(
                    Locator.ClickOptions().setTimeout(1000.0)
                )
                println("Popup zamknięty: $selector")
            } catch (e: PlaywrightException) {
                println("Nie udało się zamknąć popupu: ${e.message}")
            }
        }
    }
}
