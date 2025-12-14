package pl.edu.pjwstk.s25236.diet_app

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import pl.edu.pjwstk.s25236.diet_app.generated.api.NutrientsApi
import pl.edu.pjwstk.s25236.diet_app.generated.model.NutrientListItemDto
import pl.edu.pjwstk.s25236.diet_app.service.NutrientService

@RestController
class NutrientsController(
    private val nutrientService: NutrientService,
    private val dtoMapper: DtoMapper
) : BaseApiController(), NutrientsApi {

    override fun listNutrients(): ResponseEntity<List<NutrientListItemDto>> {
        return nutrientService.listAllNutrients()
            .map { list ->
                list.map { dtoMapper.modelMapper.map(it, NutrientListItemDto::class.java) }
            }
            .fold(
                { error -> onError(error) },
                { ok -> ResponseEntity.ok(ok) }
            )
    }
}