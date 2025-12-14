package pl.edu.pjwstk.s25236.diet_app

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.edu.pjwstk.s25236.diet_app.nutrient.GetAllNutrients
import pl.edu.pjwstk.s25236.diet_app.nutrient.NutrientRepository

@Configuration
class NutrientConfiguration {

    @Bean
    fun getNutrient(
        repository: NutrientRepository
    ): GetAllNutrients = GetAllNutrients(repository)
}