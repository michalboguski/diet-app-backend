package pl.edu.pjwstk.s25236.diet_app

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import pl.edu.pjwstk.s25236.diet_app.common.Error.Code.NUTRIENT_NOT_FOUND
import pl.edu.pjwstk.s25236.diet_app.generated.api.NutrientsApi
import pl.edu.pjwstk.s25236.diet_app.generated.model.NutrientListItemDto
import pl.edu.pjwstk.s25236.diet_app.nutrient.GetAllNutrients
import pl.edu.pjwstk.s25236.diet_app.nutrient.RetrieveNutrient

@RestController
class NutrientsController(
    private val dtoMapper: DtoMapper,
    private val getAllNutrients: GetAllNutrients,
    private val retrieveNutrient: RetrieveNutrient
) : BaseApiController(), NutrientsApi {

    override fun listNutrients(): ResponseEntity<List<NutrientListItemDto>> {
        return getAllNutrients.execute()
            .map { list ->
                list.map { dtoMapper.modelMapper.map(it, NutrientListItemDto::class.java) }
            }
            .fold(
                { error -> onError(error) },
                { ok -> ResponseEntity.ok(ok) }
            )
    }

    override fun retrieveNutrient(id: Long): ResponseEntity<NutrientListItemDto> {
        return retrieveNutrient.execute(id)
            .flatMap { maybeNutrient ->
                maybeNutrient.toEither { NUTRIENT_NOT_FOUND.toError() }
            }
            .map { nutrient ->
                dtoMapper.modelMapper.map(nutrient, NutrientListItemDto::class.java)
            }
            .fold(
                { error -> onError(error) },
                { dto -> ResponseEntity.ok(dto) }
            )
    }
}