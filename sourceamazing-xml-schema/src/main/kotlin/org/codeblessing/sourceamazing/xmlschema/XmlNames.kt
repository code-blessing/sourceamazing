package org.codeblessing.sourceamazing.xmlschema

import org.codeblessing.sourceamazing.api.process.schema.*
import org.codeblessing.sourceamazing.tools.CaseUtil

object XmlNames {

    const val FACET_SIMPLE_VALUE_ATTRIBUTE_NAME = "value"
    const val FACET_VALUE_TAG_NAME = "facetValue"
    const val CONCEPT_REF_TAG_NAME = "conceptRef"

    const val CONCEPT_IDENTIFIER_ATTRIBUTE_NAME = "conceptIdentifier"
    const val CONCEPT_IDENTIFIER_REFERENCE_ATTRIBUTE_NAME = "conceptIdentifierReference"

    fun xmlConceptName(conceptName: ConceptName): String {
        return CaseUtil.decapitalize(conceptName.clazz.java.simpleName)
    }

    fun xmlFacetName(facetName: FacetName): String {
        return CaseUtil.decapitalize(facetName.clazz.java.simpleName)
    }

    fun conceptFromXmlConceptName(xmlConceptName: String, schemaAccess: SchemaAccess): ConceptSchema? {
        return schemaAccess.allConcepts().firstOrNull { xmlConceptName(it.conceptName) == xmlConceptName }
    }

    fun facetFromXmlFacetName(xmlFacetName: String, conceptSchema: ConceptSchema): FacetSchema? {
        return conceptSchema.facets.firstOrNull { xmlFacetName(it.facetName) == xmlFacetName }
    }
}