package pl.edu.pjwstk.s25236.dietapp.nutrient

import io.vavr.control.Either
import io.vavr.control.Option
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.edu.pjwstk.s25236.dietapp.common.Error
import pl.edu.pjwstk.s25236.dietapp.common.Success

@Service
class AppNutrientService(
    private val nutrientRepository: NutrientRepository,
) : NutrientService {
    @Transactional
    override fun create(cmd: CreateNutrientCommand): Either<Error, Success> =
        nutrientRepository.create(NutrientRepository.CreateNutrientData(name = cmd.name, unit = cmd.unit))

    override fun retrieve(nutrientId: Long): Either<Error, Option<Nutrient>> = nutrientRepository.retrieve(nutrientId)

    override fun retrieve(nutrientName: String): Either<Error, Option<Nutrient>> = nutrientRepository.retrieve(nutrientName)

    override fun retrieveAll(): Either<Error, List<Nutrient>> = nutrientRepository.retrieveAll()
}
