package pl.edu.pjwstk.s25236.dietapp

import org.jooq.DSLContext

abstract class JooqRepository(
    val context: DSLContext,
    val mapper: ValueObjectMapper,
)
