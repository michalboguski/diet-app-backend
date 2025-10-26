package pl.edu.pjwstk.s25236.diet_app.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vavr.jackson.datatype.VavrModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JacksonVavrConfig {
    @Bean
    open fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .registerModule(VavrModule())
}