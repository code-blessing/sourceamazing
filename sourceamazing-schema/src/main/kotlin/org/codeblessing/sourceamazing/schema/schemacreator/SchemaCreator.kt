package org.codeblessing.sourceamazing.schema.schemacreator

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.annotations.AdditionallyKnownClasses
import org.codeblessing.sourceamazing.schema.api.exceptions.SyntaxException
import org.codeblessing.sourceamazing.schema.api.schemaaccess.SchemaErrorCode
import org.codeblessing.sourceamazing.schema.api.schemaaccess.exceptions.SchemaSyntaxException
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.ClazzKind
import org.codeblessing.sourceamazing.schema.typesafeapi.schemaaccess.TypeSafeClazzSchema
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz

object SchemaCreator {

    @Throws(SyntaxException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: KClass<*>): TypeSafeSchemaImpl {
        val clazzSchemas: MutableMap<Clazz, TypeSafeClazzSchema> = mutableMapOf()

        val clazzClassesToProcess: MutableSet<KClass<*>> = mutableSetOf(schemaDefinitionClass)
        while (clazzClassesToProcess.isNotEmpty()) {
            val clazzSchema = ClazzSchemaCreator.createClazzSchema(clazzClassesToProcess.getAndRemoveFirst())
            clazzSchemas.put(clazzSchema.clazz, clazzSchema)

            derivedClazzes(clazzSchema)
                .filterNot { it in clazzSchemas }
                .map { it.clazz }
                .let { clazzClassesToProcess.addAll(it) }
        }

        val rootClazz = schemaDefinitionClass.toClazz()
        val rootClazzSchema = clazzSchemas.getValue(rootClazz)
        checkRootClazzIsConstructible(rootClazzSchema)

        return TypeSafeSchemaImpl(rootClazz, clazzSchemas)
    }

    private fun derivedClazzes(clazzSchema: TypeSafeClazzSchema): Set<Clazz> {
        val clazz = clazzSchema.clazz
        return clazzFromAdditionallyKnownClazzModelsAnnotation(clazz) +
            clazzFromSealedClasses(clazz) +
            clazzesFromClazzProperties(clazzSchema)
    }

    private fun clazzFromAdditionallyKnownClazzModelsAnnotation(clazz: Clazz): Set<Clazz> {
        return clazz.clazz.annotations
            .filterIsInstance<AdditionallyKnownClasses>()
            .flatMap { it.classes.toList() }
            .map { it.toClazz() }
            .toSet()
    }

    private fun clazzFromSealedClasses(clazz: Clazz): Set<Clazz> {
        return if (clazz.clazz.isSealed) {
            clazz.clazz.sealedSubclasses.map { it.toClazz() }.toSet()
        } else {
            emptySet()
        }
    }

    private fun clazzesFromClazzProperties(clazzSchema: TypeSafeClazzSchema): Set<Clazz> {
        return clazzSchema.clazzProperties.map { it.clazzPropertyClazz }.toSet()
    }

    private fun checkRootClazzIsConstructible(rootClazzSchema: TypeSafeClazzSchema) {
        if (rootClazzSchema.clazzKindInformation.clazzKind == ClazzKind.ONLY_CONSTRUCTED_INSTANCE) {
            val reasons = rootClazzSchema.clazzKindInformation.clazzKindReasons
            throw SchemaSyntaxException(
                SchemaErrorCode.ROOT_CLASS_MUST_BE_INSTANTIATABLE.withFormattedMessage(
                    rootClazzSchema.clazz.clazz,
                    reasons.joinToString("\n") { it.message },
                ),
                reasons,
            )
        }
    }

    private fun <T> MutableSet<T>.getAndRemoveFirst(): T {
        val first = this.first()
        this.remove(first)
        return first
    }
}
