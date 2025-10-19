package pl.edu.pjwstk.s25236.dietapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DietAppBackendApplication

fun main(args: Array<String>) {
    runApplication<DietAppBackendApplication>(*args)
}
