package pl.edu.pjwstk.s25236.diet_app

import org.jooq.DSLContext

abstract class JooqRepository(val context: DSLContext, val mapper: ValueObjectMapper) {
}