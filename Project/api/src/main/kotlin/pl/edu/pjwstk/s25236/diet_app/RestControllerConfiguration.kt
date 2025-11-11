package pl.edu.pjwstk.s25236.diet_app

import io.vavr.API
import org.modelmapper.ModelMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@ComponentScan()
class RestControllerConfiguration() : WebMvcConfigurer {

    @Bean
    fun modelMapper(): ModelMapper = ModelMapper().apply {
        registerModule(DietAppGeneratorModule(API.List()))
    }
}
