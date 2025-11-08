package pl.edu.pjwstk.s25236.diet_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class DietAppBackend
    fun main(args: Array<String>) {
        runApplication<DietAppBackend>(*args)
    }