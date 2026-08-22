package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.ServiceWorkerPolicy
import java.nio.file.Files
import java.nio.file.Paths

class PlaywrightBrowser : AutoCloseable {
    private val playwright = Playwright.create()
    private val browser =
        playwright
            .firefox()
            .launch(BrowserType.LaunchOptions().setHeadless(true))

    fun newPage(): Page {
        val statePath = Paths.get("state.json")
        val context =
            if (Files.exists(statePath)) {
                browser.newContext(
                    Browser
                        .NewContextOptions()
                        .setStorageStatePath(statePath)
                        .setServiceWorkers(ServiceWorkerPolicy.BLOCK),
                )
            } else {
                browser.newContext(
                    Browser
                        .NewContextOptions()
                        .setServiceWorkers(ServiceWorkerPolicy.BLOCK),
                )
            }
        return context.newPage()
    }

    override fun close() {
        browser.close()
        playwright.close()
    }
}
