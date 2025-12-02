package org.codeblessing.sourceamazing.schema.api.datacollection

import kotlin.reflect.KProperty
import org.codeblessing.sourceamazing.schema.api.ConceptNameAndIdentifier
import org.codeblessing.sourceamazing.schema.api.toFacetName

fun ConceptData.hasFacet(facetName: String): Boolean = hasFacet(facetName.toFacetName())

fun ConceptData.hasFacet(facetName: KProperty<*>): Boolean = hasFacet(facetName.name)

fun ConceptData.getFacet(facetName: String): List<Any> = getFacet(facetName.toFacetName())

fun ConceptData.getFacet(facetName: KProperty<*>): List<Any> = getFacet(facetName.name)

fun ConceptData.replaceFacetValues(facetName: String, facetValues: List<Any>): ConceptData =
    replaceFacetValues(facetName.toFacetName(), facetValues)

fun ConceptData.replaceFacetValues(facetName: KProperty<*>, facetValues: List<Any>): ConceptData =
    replaceFacetValues(facetName.name, facetValues)

fun ConceptData.addFacetValue(facetName: String, facetValue: Any): ConceptData =
    addFacetValue(facetName.toFacetName(), facetValue)

fun ConceptData.addFacetValue(facetName: KProperty<*>, facetValue: Any): ConceptData =
    addFacetValue(facetName.name, facetValue)

fun ConceptData.addFacetValues(facetName: String, facetValues: List<Any>): ConceptData =
    addFacetValues(facetName.toFacetName(), facetValues)

fun ConceptData.addFacetValues(facetName: KProperty<*>, facetValues: List<Any>): ConceptData =
    addFacetValues(facetName.name, facetValues)

fun ConceptData.toConceptNameAndIdentifier(): ConceptNameAndIdentifier =
    ConceptNameAndIdentifier(conceptName, conceptIdentifier)
