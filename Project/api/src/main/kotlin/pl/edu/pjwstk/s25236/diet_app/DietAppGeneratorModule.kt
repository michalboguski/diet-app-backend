package pl.edu.pjwstk.s25236.diet_app

import io.vavr.Value
import io.vavr.collection.List
import io.vavr.collection.Traversable
import org.modelmapper.Converter
import org.modelmapper.ModelMapper
import org.modelmapper.Module
import org.modelmapper.internal.typetools.TypeResolver.resolveRawArgument
import org.modelmapper.internal.util.Types.rawTypeFor
import org.modelmapper.spi.ConditionalConverter
import org.modelmapper.spi.MappingContext
import java.lang.reflect.ParameterizedType

class DietAppGeneratorModule(
    private val customConverters: Traversable<Converter<*, *>>
) : Module {

    override fun setupModule(modelMapper: ModelMapper?) {
        modelMapper?.run {
            configuration.converters.addAll(0, listOf(CollectionConverter()))
            customConverters.forEach { addConverter(it)}
        }
    }

    companion object {

        fun genericContext(
            context: MappingContext<*, *>,
            source: Any
        ): MappingContext<*, *> {
            val genericType = context.genericDestinationType

            return if (genericType is ParameterizedType) {
                val rawType = rawTypeFor(genericType.actualTypeArguments[0])
                context.create(source, rawType)
            } else {
                val destination = context.mapping.lastDestinationProperty
                val rawType = resolveRawArgument(destination.genericType, destination.initialType)
                context.create(source, rawType)
            }
        }

        class CollectionConverter : ConditionalConverter<Value<*>, List<*>> {
            override fun match(
                sourceType: Class<*>?,
                destinationType: Class<*>?
            ): ConditionalConverter.MatchResult {
                if (sourceType == null || destinationType == null) {
                    return ConditionalConverter.MatchResult.NONE
                }
                return if (
                    (io.vavr.collection.List::class.java.isAssignableFrom(sourceType))
                    || (io.vavr.collection.Set::class.java.isAssignableFrom(sourceType))
                ) ConditionalConverter.MatchResult.FULL
                else ConditionalConverter.MatchResult.NONE
            }

            override fun convert(context: MappingContext<Value<*>?, List<*>?>?): List<*>? {
                val ctx = context ?: return List.empty<Any>()
                val source = ctx.source ?: return List.empty<Any>()
                val result = source
                    .map { source ->
                        ctx.mappingEngine.map(genericContext(ctx as MappingContext<*, *>, source))
                    }
                    .toJavaList()

                return List.ofAll(result)
            }
        }
    }
}
