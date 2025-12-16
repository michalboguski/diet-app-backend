package pl.edu.pjwstk.s25236.dietapp

import org.springframework.stereotype.Component
import pl.edu.pjwstk.s25236.dietapp.nutrient.Nutrient
import pl.edu.pjwstk.s25236.dietapp.nutrient.NutrientRepository.CreateNutrientData
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.records.NutrientRecord
import pl.edu.pjwstk.s25236.dietgenerator.jooq.tables.references.NUTRIENT

@Component
class ValueObjectMapper {
    private fun <T : Any> get(value: T?): T = requireNotNull(value) { "NULL in required field: check database" }

    fun toNutrient(record: NutrientRecord): Nutrient =
        Nutrient(
            id = get(record.id),
            name = get(record.name),
            unit = get(record.unit),
        )

    fun toNutrientRecord(data: CreateNutrientData): NutrientRecord = NutrientRecord().with(NUTRIENT.NAME, data.name)
}
