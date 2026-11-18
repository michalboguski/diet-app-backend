package pl.edu.pjwstk.s25236.dietapp.scrap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.playwright.Page
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
    val source: RecipeSource
    val page: Page
    val paths: ServicePaths

    fun prepareServicePaths(service: String): ServicePaths {

        val dataDirectory = Paths.get("data").toAbsolutePath().normalize()
        val directory = dataDirectory.resolve(service)
        val paths = ServicePaths(
            directory = directory,
            state = directory.resolve("state.json"),
            links = directory.resolve("links.txt"),
            recipes = directory.resolve("recipes"),
            pages = directory.resolve("pages"),
            progress = dataDirectory.resolve("scraper-progress.json"),
        )
        Files.createDirectories(paths.directory)
        Files.createDirectories(paths.recipes)
        Files.createDirectories(paths.pages)
        Files.newByteChannel(paths.links, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { }
        if (!Files.exists(paths.progress)) {
            Files.writeString(
                paths.progress,
                "{}",
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
            )
        }

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

    override fun close() {
        browser.closePage(this, page)
    }

    fun getLinksFromRange(
        baseLink: String,
        start: Int,
        end: Int,
    ): List<String> {
        val progressStore = ProgressStore(paths.progress)
        val progress = progressStore.read()[source.service]
            ?: ScraperProgress(
                baseLink = baseLink,
                nextPage = start,
                endPage = end,
            )
        return browser.use { browser ->
            this.use { scraper ->
                val path = this.paths.links
                val links = LinkedHashSet<String>()
                if (Files.notExists(path)) {
                    Files.createFile(path)
                }
                Files.newBufferedReader(path, UTF_8).use { reader ->
                    reader.lineSequence().filter { it.isNotBlank() }.forEach { links.add(it) }
                }

                for ((index, num) in (progress.nextPage..progress.endPage).withIndex()) {
                    if (Thread.currentThread().isInterrupted) {
                        throw InterruptedException("Przerwano pobieranie linków")
                    }

                    val sleepMillis = when {
                        index % 25 == 1 -> 5000
                        index % 5 == 1 -> 1000
                        else -> 0
                    }.toLong()

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
                    progressStore.save(
                        scraperId = source.service,
                        progress = progress.copy(nextPage = num + 1),
                    )
                    println("${paths.directory} $baseLink $num ")

                    progressStore.save(
                        scraperId = source.service,
                        progress = progress.copy(nextPage = num + 1),
                    )
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
                Files.readAllLines(paths.links)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .filterNot { link ->
                        Files.exists(paths.pages.resolve("${extractName(link)}.html"))
                    }.let { remainingLinks ->
                        remainingLinks.forEachIndexed { index, link ->
                            val startTime = System.nanoTime()
                            try {
                                when {
                                    index % 25 == 1 -> Thread.sleep(5000)
                                    index % 5 == 1 -> Thread.sleep(1000)
                                }
                                getRecipeFromLink(link)
                            } catch (e: InterruptedException) {
                                Thread.currentThread().interrupt()
                                throw e
                            } catch (e: Exception) {
                                print("ERROR: ${e.message}")
                            } finally {
                                val seconds = (System.nanoTime() - startTime) / 1_000_000_000.0
                                print("time: %.2f s".format(seconds))
                                println(" ${index + 1}/${remainingLinks.size} : $link")
                            }
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

    fun saveRawRecipe(
        recipe: RawRecipe,
    ) {

        val file = Path.of(paths.recipes.toString(), "${recipe.title}.txt")
        val separator = "==========="

        val content = buildString {
            appendLine(recipe.link)
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
    val progress: Path
)

data class ScraperLinkProgress(
    val baseLink: String,
    val nextPage: Int,
    val endPage: Int,
)

data class ScraperProgress(
    val baseLink: String,
    val nextPage: Int,
    val endPage: Int,
)

class ProgressStore(path: Path) {

    companion object {
        private val lock = Any()
    }

    private val path = path.toAbsolutePath()
    private val mapper = jacksonObjectMapper()

    fun read(): Map<String, ScraperProgress> = synchronized(lock) {
        if (!Files.exists(path)) {
            return@synchronized emptyMap()
        }

        Files.newBufferedReader(path).use { reader ->
            mapper.readValue<Map<String, ScraperProgress>>(reader)
        }
    }

    fun save(scraperId: String, progress: ScraperProgress) {
        synchronized(lock) {
            val allProgress = read().toMutableMap()
            allProgress[scraperId] = progress

            Files.createDirectories(path.parent)

            val temporaryPath = Files.createTempFile(
                path.parent,
                "scraper-progress-",
                ".tmp",
            )

            try {
                Files.newBufferedWriter(temporaryPath).use { writer ->
                    mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(writer, allProgress)
                }

                Files.move(
                    temporaryPath,
                    path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE,
                )
            } finally {
                Files.deleteIfExists(temporaryPath)
            }
        }
    }
}
