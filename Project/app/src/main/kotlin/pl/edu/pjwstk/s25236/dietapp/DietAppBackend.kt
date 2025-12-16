package pl.edu.pjwstk.s25236.dietapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(value = [RestControllerConfiguration::class])
class DietAppBackend

fun main(args: Array<String>) {
    runApplication<DietAppBackend>(*args)
}
