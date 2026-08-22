package pl.edu.pjwstk.s25236.dietapp.ingredient

import io.vavr.control.Either
import io.vavr.control.Option
import org.springframework.stereotype.Service
import pl.edu.pjwstk.s25236.dietapp.common.Error
import pl.edu.pjwstk.s25236.dietapp.common.Success

@Service
class IngredientAppService(
    private val ingredientRepository: IngredientRepository,
) : IngredientService {
    override fun retrieveAll(): Either<Error, List<Ingredient>> = ingredientRepository.retrieveAll()

    override fun retrieve(ingredientId: Long): Either<Error, Option<Ingredient>> = ingredientRepository.retrieve(ingredientId)
}
