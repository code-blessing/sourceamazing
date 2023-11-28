package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.*
import org.codeblessing.sourceamazing.api.process.schema.annotations.*
import org.codeblessing.sourceamazing.api.process.schema.exceptions.MalformedSchemaException
import java.lang.reflect.Method
import kotlin.jvm.Throws
import kotlin.reflect.KClass

object SchemaCreator {

    @Throws(MalformedSchemaException::class)
    fun createSchemaFromSchemaDefinitionClass(schemaDefinitionClass: Class<*>): SchemaImpl {
        validateTypeAnnotation(annotation = Schema::class.java, classToInspect = schemaDefinitionClass)

        if(hasClassAnnotation(annotation = Concept::class.java, classToInspect = schemaDefinitionClass)) {
            throw MalformedSchemaException("Definition class '${schemaDefinitionClass.name}' can not be a concept having an annotation of type '${Concept::class.java.name}'")
        }

        val concepts: MutableMap<ConceptName, ConceptSchema> = mutableMapOf()

        schemaDefinitionClass.methods.forEach { method ->
            if(!supportForChildConceptMethod(concepts, schemaDefinitionClass, method, null)) {
                throw MalformedSchemaException("Schema definition class '${schemaDefinitionClass.name}' can " +
                        "only have methods annotated with '${ChildConcepts::class.qualifiedName}'. Not valid for method '$method'.")

            }
        }

        return SchemaImpl(concepts)
    }

    private fun validateChildConceptMethod(definitionClass: Class<*>, method: Method) {
        if(method.returnType != List::class.java) {
            throw MalformedSchemaException("The method '$method' on the definition class '${definitionClass.name}' " +
                    "must return a '${List::class.java}' but is returning a '${method.returnType}'.")
        }
    }

    private fun validateAndAddConcept(concepts: MutableMap<ConceptName, ConceptSchema>, conceptClass: Class<*>, parentConceptName: ConceptName?) {
        validateTypeAnnotation(annotation = Concept::class.java, classToInspect = conceptClass)
        val conceptName = ConceptName.of(conceptClass.getAnnotation(Concept::class.java).conceptName)

        validateConceptItselfNotPresentInUpperHierarchy(concepts, conceptName, parentConceptName)

        val parentConceptSchema = parentConceptName?.let { concepts[parentConceptName] }
        addConceptSchema(concepts, conceptName, conceptClass, parentConceptSchema)

        val supportedConceptAnnotations = supportedConceptAnnotations(parentConceptSchema != null)

        conceptClass.methods.forEach { method ->
            if(!supportForChildConceptMethod(concepts, conceptClass, method, conceptName) && !hasSupportedConceptAnnotation(method, supportedConceptAnnotations)) {
                throw MalformedSchemaException(
                    "Concept definition class '${conceptClass.name}' can " +
                            "only have methods annotated with ${supportedConceptAnnotations.map { "'${it.kotlin.qualifiedName}'" }.joinToString(" or ")}. " +
                            "Not valid for method '$method'."
                )
            }
        }
    }

    private fun validateConceptItselfNotPresentInUpperHierarchy(concepts: MutableMap<ConceptName, ConceptSchema>, conceptName: ConceptName, parentConceptName: ConceptName?) {
        if(parentConceptName == null) {
            // root concept
            return
        }

        if(conceptName == parentConceptName) {
            throw MalformedSchemaException("Concept '${conceptName.name}' can not be its own parent.")
        }

        val allHierarchicalBranches = allHierarchyPathsToRoot(concepts, parentConceptName)
        allHierarchicalBranches.forEach { parentConceptHierarchy ->
            if(parentConceptHierarchy.contains(conceptName)) {
                throw MalformedSchemaException("There is a cyclic dependency with concept '${conceptName.name}'. Parent concepts in a hierarchy must be all different concepts, but was $parentConceptHierarchy.")
            }
        }
    }

    private fun allHierarchyPathsToRoot(concepts: Map<ConceptName, ConceptSchema>, conceptName: ConceptName): Set<List<ConceptName>> {
        val conceptSchema = concepts[conceptName] ?: throw IllegalStateException("Concept '${conceptName.name}' not found.")

        val resultSet: MutableSet<List<ConceptName>> = mutableSetOf()
        conceptSchema.parentConceptNames.forEach { parentConceptName ->
            allHierarchyPathsToRoot(concepts, parentConceptName).forEach { allBranchesOfParent ->
                resultSet.add(listOf(conceptName) + allBranchesOfParent)
            }
        }

        if(resultSet.isEmpty()) {
            // if it is a root concept, there is one branch from itself to the root
            return setOf(listOf(conceptName))
        }

        return resultSet
    }

    private fun hasSupportedConceptAnnotation(method: Method, supportedConceptAnnotations: List<Class<out Annotation>>): Boolean {
        return supportedConceptAnnotations.any { supportedAnnotation: Class<out Annotation> -> hasMethodAnnotation(supportedAnnotation, method) }
    }

    private fun supportedConceptAnnotations(hasParent: Boolean): List<Class<out Annotation>> {
        return listOfNotNull(
            ChildConcepts::class.java,
            ChildConceptsWithCommonBaseInterface::class.java,
            Facet::class.java,
            ConceptId::class.java,
        )
    }

    private fun supportForChildConceptMethod(concepts: MutableMap<ConceptName, ConceptSchema>, definitionClass: Class<*>, method: Method, parentConceptName: ConceptName?): Boolean {
        if(hasMethodAnnotation(ChildConcepts::class.java, method)) {
            validateAndAddConceptForChildConceptsAnnotation(concepts, definitionClass, method, parentConceptName)
            return true
        } else if(hasMethodAnnotation(ChildConceptsWithCommonBaseInterface::class.java, method)) {
            validateAndAddConceptForChildConceptsWithCommonBaseInterfaceAnnotation(concepts, definitionClass, method, parentConceptName)
            return true
        }
        return false
    }

    private fun validateAndAddConceptForChildConceptsAnnotation(concepts: MutableMap<ConceptName, ConceptSchema>, definitionClass: Class<*>, method: Method, parentConceptName: ConceptName?) {
        validateChildConceptMethod(definitionClass, method)
        val conceptClass = method.getAnnotation(ChildConcepts::class.java).conceptClass.java
        validateAndAddConcept(concepts, conceptClass = conceptClass, parentConceptName = parentConceptName)
    }

    private fun validateAndAddConceptForChildConceptsWithCommonBaseInterfaceAnnotation(concepts: MutableMap<ConceptName, ConceptSchema>, definitionClass: Class<*>, method: Method, parentConceptName: ConceptName?) {
        validateChildConceptMethod(definitionClass, method)
        val baseInterfaceClass = method.getAnnotation(ChildConceptsWithCommonBaseInterface::class.java).baseInterfaceClass.java
        val conceptClasses = method.getAnnotation(ChildConceptsWithCommonBaseInterface::class.java).conceptClasses.map { it.java }
        conceptClasses.forEach { conceptClass ->
            if(!isInheriting(conceptClass, baseInterfaceClass)) {
                throw MalformedSchemaException("Class ${conceptClass.name} must inherit base class ${baseInterfaceClass.name} in method $method.")
            }
            validateAndAddConcept(concepts, conceptClass = conceptClass, parentConceptName = parentConceptName)
        }
    }

    private fun isInheriting(subInterface: Class<*>, baseInterface: Class<*>): Boolean {
        // TODO this only supports direct baseInterfaces
        return subInterface.interfaces.contains(baseInterface)
    }

    private fun addConceptSchema(
        concepts: MutableMap<ConceptName, ConceptSchema>,
        conceptName: ConceptName,
        conceptClass: Class<*>,
        parentConceptSchema: ConceptSchema?
    ) {
        val isRootConcept = parentConceptSchema == null
        val alreadyExistingConcept = concepts[conceptName]
        if(alreadyExistingConcept != null) {
            val newParentConceptNames = if (parentConceptSchema != null) {
                alreadyExistingConcept.parentConceptNames + parentConceptSchema.conceptName
            } else {
                alreadyExistingConcept.parentConceptNames
            }

            concepts[conceptName] = ConceptSchemaImpl(
                conceptName = conceptName,
                conceptClass = conceptClass,
                isRootConcept = isRootConcept || alreadyExistingConcept.isRootConcept,
                parentConceptNames = newParentConceptNames,
                facets = alreadyExistingConcept.facets,
                minOccurrence = alreadyExistingConcept.minOccurrence,
                maxOccurrence = alreadyExistingConcept.maxOccurrence,
            )
        } else {
            val facets = gatherFacets(conceptClass)
            val minOccurrence = conceptClass.getAnnotation(Concept::class.java).minOccurrence;
            val maxOccurrence = conceptClass.getAnnotation(Concept::class.java).maxOccurrence;

            val newParentConceptNames = if (parentConceptSchema != null) {
                setOf(parentConceptSchema.conceptName)
            } else {
                emptySet()
            }

            concepts[conceptName] = ConceptSchemaImpl(
                conceptName = conceptName,
                conceptClass = conceptClass,
                isRootConcept = isRootConcept,
                parentConceptNames = newParentConceptNames,
                facets = facets,
                minOccurrence = minOccurrence,
                maxOccurrence = maxOccurrence
            )

        }

    }

    private fun gatherFacets(conceptClass: Class<*>): List<FacetSchema> {
        val facets = mutableListOf<FacetSchema>()
        conceptClass.methods.forEach { method ->
            val returnType = method.returnType.kotlin
            if(hasMethodAnnotation(Facet::class.java, method)) {
                val facetName = FacetName.of(method.getAnnotation(Facet::class.java).facetName)
                val isMandatory = method.getAnnotation(Facet::class.java).mandatory
                val facetType: FacetTypeEnum = FacetTypeEnum.matchingEnumByTypeClass(returnType)
                    ?: throw MalformedSchemaException("Return type '$returnType' of method '$method' does not match any compatible facet types (${FacetTypeEnum.supportedTypes()}).")
                val referencingConcept = FacetTypeEnum.referencedTypeConceptName(returnType)
                val enumerationType = validatedEnumerationType(facetName, facetType, method)
                val facet = FacetSchemaImpl(facetName, facetType, mandatory = isMandatory, referencingConcept = referencingConcept, enumerationType = enumerationType)
                val alreadyExistingFacet = facets.firstOrNull { it.facetName == facetName }
                if(alreadyExistingFacet == null) {
                    facets.add(facet)
                } else {
                    if(alreadyExistingFacet != facet) {
                        throw MalformedSchemaException("You try to define the facet '${facetName.name}' multiple " +
                                "times but with different parameters. Facet $facet in method '$method' does not " +
                                "match already existing facet $alreadyExistingFacet.")
                    }
                }
            } else if(hasMethodAnnotation(ConceptId::class.java, method)) {
                if(returnType != String::class && returnType != ConceptIdentifier::class) {
                    throw MalformedSchemaException("The return type of a method with ${ConceptId::class.qualifiedName} " +
                            "must be ${String::class.qualifiedName} or ${ConceptIdentifier::class.qualifiedName} " +
                            "in method '$method'.")
                }
            }
        }
        return facets
    }

    private fun validatedEnumerationType(facetName: FacetName, facetType: FacetTypeEnum, method: Method): KClass<*>? {
        if(facetType != FacetTypeEnum.TEXT_ENUMERATION) {
            return null
        }

        val enumerationType = method.returnType.kotlin
        val enumConstants = enumerationType.java.enumConstants
            ?: throw MalformedSchemaException("You try to define the facet '${facetName.name}' " +
                    "as enumeration but the return type of method '$method' is not an enum but '${enumerationType.qualifiedName}'.")

        val enumValuesButNotEnum = enumConstants.filterNot { it is Enum<*> }
        if(enumValuesButNotEnum.isNotEmpty()) {
            throw MalformedSchemaException("You try to define the facet '${facetName.name}' " +
                    "as enumeration but the following enum values [${enumValuesButNotEnum.joinToString(",")}] are not of " +
                    "type ${Enum::class.qualifiedName} for method '$method'.")
        }

        return enumerationType;
    }

    private fun validateTypeAnnotation(annotation: Class<out Annotation>, classToInspect: Class<*>) {
        if(!classToInspect.isInterface) {
            throw MalformedSchemaException("Definition class '${classToInspect.name}' must be an interface.")
        }
        if(!hasClassAnnotation(annotation, classToInspect)) {
            throw MalformedSchemaException("Definition class '${classToInspect.name}' must have an annotation of type '${annotation.name}'")
        }
    }

    private fun hasClassAnnotation(annotation: Class<out Annotation>, classToInspect: Class<*>): Boolean {
        return classToInspect.getAnnotation(annotation) != null
    }

    private fun hasMethodAnnotation(annotation: Class<out Annotation>, methodToInspect: Method): Boolean {
        return methodToInspect.getAnnotation(annotation) != null
    }

}
