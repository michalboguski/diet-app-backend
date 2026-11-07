package pl.edu.pjwstk.s25236.dietapp.scrap

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.options.WaitUntilState
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.text.Charsets.UTF_8
import kotlin.text.substringBefore
import kotlin.use

interface RecipeScraper : AutoCloseable {
    val browser: PlaywrightBrowser
    val service: String
    val page: Page
    val paths: ServicePaths

    fun prepareServicePaths(service: String): ServicePaths {

        val directory = Paths.get("data").toAbsolutePath().normalize().resolve(service)
        val paths = ServicePaths(
            directory = directory,
            state = directory.resolve("state.json"),
            links = directory.resolve("links.txt"),
            recipes = directory.resolve("recipes"),
            pages = directory.resolve("pages"),
        )
        Files.createDirectories(paths.directory)
        Files.createDirectories(paths.recipes)
        Files.createDirectories(paths.pages)
        Files.newByteChannel(paths.links, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { }
        return paths
    }

    fun extractName(link: String): String {
        return link.substringBefore('?').substringBefore('#').trimEnd('/').substringAfterLast('/').replace("-", " ")
            .replace("_", " ")
    }

    fun getLinksFromPage(baseLink: String, pageNumber: Int): List<String>

    fun getRecipe(link: String): RawRecipe

    fun buildPageUrl(
        base: String,
        page: Int,
    ): String

    fun openPage(
        page: Page,
        link: String,
    ): Page {
        page.navigate(
            link,
            Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(20_000.0),
        )
        clickRodo(page, service)
        page.tryClosePopups()
        return page
    }

    override fun close() {
        browser.closePage(this, page)
    }

    fun clickRodo(
        page: Page,
        service: String,
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
                    button.click(
                        Locator.ClickOptions().setTimeout(1500.0)
                    )
                    val statePath = Paths.get(
                        "states", "$service.json"
                    )
                    Files.createDirectories(statePath.parent)

                    page.context().storageState(
                        BrowserContext.StorageStateOptions().setPath(statePath)
                    )
                    println("Zaakceptowano RODO: $selector")
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

    fun getLinksFromRange(
        baseLink: String,
        start: Int,
        end: Int,
    ): List<String> {
        return browser.use { browser ->
            this.use { scraper ->
                val path = paths.links
                val links = LinkedHashSet<String>()
                if (Files.notExists(path)) {
                    Files.createFile(path)
                }
                Files.newBufferedReader(path, UTF_8).use { reader ->
                    reader.lineSequence().filter { it.isNotBlank() }.forEach { links.add(it) }
                }

                for ((index, num) in (start..end).withIndex()) {
                    if (Thread.currentThread().isInterrupted) {
                        throw InterruptedException("Przerwano pobieranie linków")
                    }

                    val sleepMillis = when {
                        index % 25 == 0 -> 11000
                        index % 5 == 0 -> 2000
                        else -> 0
                    }.toLong()

                    println("page: $num sleep: ${sleepMillis / 1000} second")

                    if (sleepMillis > 0) {
                        try {
                            Thread.sleep(sleepMillis)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            throw e
                        }
                    }

                    val downloaded = scraper.getLinksFromPage(baseLink, num)
                    downloaded.filter { it.isNotBlank() }.forEach { link ->
                        links.add(link)
                    }
                    saveLinks(path, links)
                }
                links.toList()
            }
        }
    }

    private fun saveLinks(path: Path, links: Set<String>) {
        val temporary = Files.createTempFile(path.parent, "links-", ".tmp")
        try {
            Files.newBufferedWriter(temporary, UTF_8).use { writer ->
                for (link in links) {
                    writer.write(link)
                    writer.newLine()
                }
            }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (failure: Exception) {
            try {
                Files.deleteIfExists(temporary)
            } catch (cleanupFailure: Exception) {
                failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
    }

    fun processLinks() {
        browser.use {
            this.use {
                var counter = 0
                Files.readAllLines(paths.links).map { it.trim() }.filter { it.isNotBlank() }.forEach { link ->
                    val file = paths.pages.resolve("${extractName(link)}.html")
                    if (Files.exists(file)) {
                        println("skip: $link")
                        return@forEach
                    }
                    try {
                        when {
                            counter % 25 == 0 -> Thread.sleep(11000)
                            counter % 5 == 0 -> Thread.sleep(2000)
                        }
                        counter++
                        println("processed: $link")
                        getRecipeFromLink(link)
                    } catch (e: Exception) {
                        println("ERROR: ${e.message}")
                    }

                }
            }
        }
    }

    fun getRecipeFromLink(link: String) {
        val rawRecipe = getRecipe(link)
        saveRecipePage()
        saveRawRecipe(rawRecipe)
    }

    private fun saveRecipePage() {
        val name = extractName(page.url())
        val file = Path.of(paths.pages.toString(), "$name.html")
        Files.writeString(file, page.content())
    }

    private fun saveRawRecipe(
        recipe: RawRecipe,
    ) {

        val file = Path.of(paths.recipes.toString(), "${recipe.title}.txt")
        val separator = "==========="

        val content = buildString {
            recipe.ingredientLines.forEach { appendLine(it) }
            appendLine(separator)
            recipe.stepLines.forEach { appendLine(it) }
        }

        Files.writeString(
            file,
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
        )
    }
}

data class ServicePaths(
    val directory: Path,
    val state: Path,
    val links: Path,
    val recipes: Path,
    val pages: Path,
)
