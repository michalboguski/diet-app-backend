package pl.edu.pjwstk.s25236.dietapp

import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class DtoMapper(
    val modelMapper: ModelMapper,
)
