package pl.edu.pjwstk.s25236.diet_app

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.edu.pjwstk.s25236.diet_app.nutrient.GetAllNutrients
import pl.edu.pjwstk.s25236.diet_app.nutrient.NutrientRepository
import pl.edu.pjwstk.s25236.diet_app.nutrient.RetrieveNutrient

@Configuration
class NutrientConfiguration {

    @Bean
    fun getAllNutrients(
        repository: NutrientRepository
    ): GetAllNutrients = GetAllNutrients(repository)

    @Bean
    fun getNutrient(
        repository: NutrientRepository
    ): RetrieveNutrient = RetrieveNutrient(repository)
}