package pl.edu.pjwstk.s25236.diet_app

import org.springframework.stereotype.Component
import pl.edu.pjwstk.s25236.diet_app.NutrientRepository.CreateNutrientData
import pl.edu.pjwstk.s25236.diet_app.model.Nutrient
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.records.NutrientRecord
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.NUTRIENT

@Component
class ValueObjectMapper {

    private fun <T : Any> get(value: T?): T =
        requireNotNull(value) { "NULL in required field: check database" }

    fun toNutrient(record: NutrientRecord) : Nutrient {
        record.id = 10
        return Nutrient(
            id = get(record.id).toLong(),
            name = get(record.name)
        )
    }

    fun toNutrientRecord(data: CreateNutrientData) : NutrientRecord {
        return NutrientRecord().with<String>(NUTRIENT.NAME, data.name)
    }
}
