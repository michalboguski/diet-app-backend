package pl.edu.pjwstk.s25236.diet_app.service

import io.vavr.control.Either
import org.springframework.stereotype.Service
import pl.edu.pjwstk.s25236.diet_app.model.Error
import pl.edu.pjwstk.s25236.diet_app.model.Nutrient

@Service
class DomainNutrientService(private val nutrientRepository: NutrientRepository) : NutrientService {

    override fun listAllNutrients(): Either<Error, List<Nutrient>> {
        return nutrientRepository.retrieveAll()
    }
}