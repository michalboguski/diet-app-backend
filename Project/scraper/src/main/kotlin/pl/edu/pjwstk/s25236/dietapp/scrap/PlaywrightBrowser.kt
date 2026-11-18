package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.options.ServiceWorkerPolicy
import com.microsoft.playwright.options.WaitUntilState
import java.net.URI
import java.nio.file.Files

class PlaywrightBrowser : AutoCloseable {
    private val playwright = Playwright.create()
    private val browser = playwright.firefox().launch(BrowserType.LaunchOptions().setHeadless(true))

    fun newPage(recipeScraper: RecipeScraper): Page {
        val statePath = recipeScraper.paths.state
        val options =
            Browser.NewContextOptions().setServiceWorkers(ServiceWorkerPolicy.BLOCK).setJavaScriptEnabled(false)

        if (Files.exists(statePath)) {
            options.setStorageStatePath(statePath)
        }

        val context = browser.newContext(options)
        context.blockAds()
        return context.newPage()
    }

    fun openPage(
        page: Page,
        link: String,
    ): Page {

        page.navigate(
            link,
            Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT).setTimeout(20_000.0),
        )
        // clickRodo(page)
        //  page.tryClosePopups()
        return page
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

    fun clickRodo(
        page: Page
    ): Boolean {
        val selectors = listOf(
            "#accept-btn",
            "#onetrust-accept-btn-handler",
            "#didomi-notice-agree-button",
            "button:has-text('Akceptuję')",
            "button:has-text('Akceptuj wszystkie')",
            "button:has-text('Zaakceptuj wszystkie')",
            "button:has-text('Zgadzam się')",
            "button:has-text('Zgadzam się na wszystkie')",
            "button:has-text('Accept all')",
            "button:has-text('Allow all')",
            "button:has-text('I agree')",
            "button:has-text('Agree and close')",
            "[data-testid='uc-accept-all-button']",
            "[data-testid='accept-all']",
        )
        val frames = page.frames()

        for (frame in frames) {
            for (selector in selectors) {
                try {
                    val button = frame.locator(selector).first()
                    if (!button.isVisible) continue
                    button.click(Locator.ClickOptions().setTimeout(1500.0))
                    return true
                } catch (e: PlaywrightException) {
                    println(
                        "Nie udało się kliknąć $selector: ${e.message}"
                    )
                }
            }
        }
        return false
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
