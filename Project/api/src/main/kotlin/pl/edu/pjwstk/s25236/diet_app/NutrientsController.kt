package pl.edu.pjwstk.s25236.diet_app

import org.springframework.http.ResponseEntity
import pl.edu.pjwstk.s25236.diet_app.generated.api.NutrientsApi
import pl.edu.pjwstk.s25236.diet_app.generated.model.NutrientListItemDto

class NutrientsController : NutrientsApi {
    override fun listNutrients(): ResponseEntity<List<NutrientListItemDto>> {
        return super.listNutrients()
    }
}