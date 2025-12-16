package pl.edu.pjwstk.s25236.dietapp

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import pl.edu.pjwstk.s25236.dietapp.common.Error.Code.NUTRIENT_NOT_FOUND
import pl.edu.pjwstk.s25236.dietapp.generated.api.NutrientsApi
import pl.edu.pjwstk.s25236.dietapp.generated.model.NutrientListItemDto
import pl.edu.pjwstk.s25236.dietapp.nutrient.NutrientService

@RestController
class NutrientsController(
    private val dtoMapper: DtoMapper,
    private val nutrientService: NutrientService,
) : BaseApiController(),
    NutrientsApi {
    override fun listNutrients(): ResponseEntity<List<NutrientListItemDto>> =
        nutrientService
            .retrieveAll()
            .map { list ->
                list.map { dtoMapper.modelMapper.map(it, NutrientListItemDto::class.java) }
            }.fold(
                { error -> onError(error) },
                { ok -> ResponseEntity.ok(ok) },
            )

    override fun retrieveNutrient(id: Long): ResponseEntity<NutrientListItemDto> =
        nutrientService
            .retrieve(id)
            .flatMap { maybeNutrient ->
                maybeNutrient.toEither { NUTRIENT_NOT_FOUND.toError() }
            }.map { nutrient ->
                dtoMapper.modelMapper.map(nutrient, NutrientListItemDto::class.java)
            }.fold(
                { error -> onError(error) },
                { dto -> ResponseEntity.ok(dto) },
            )
}
