package org.codeblessing.sourceamazing.schema.api.datacollection

import kotlin.reflect.KClass
import org.codeblessing.sourceamazing.schema.api.ConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.ConceptName
import org.codeblessing.sourceamazing.schema.api.toConceptIdentifier
import org.codeblessing.sourceamazing.schema.api.toConceptName

fun ConceptDataCollector.existingConceptData(conceptIdentifier: String): ConceptData =
    existingConceptData(conceptIdentifier.toConceptIdentifier())

fun ConceptDataCollector.existingOrNewConceptData(
    conceptName: KClass<*>,
    conceptIdentifier: ConceptIdentifier,
): ConceptData = existingOrNewConceptData(conceptName.toConceptName(), conceptIdentifier)

fun ConceptDataCollector.existingOrNewConceptData(conceptName: KClass<*>, conceptIdentifier: String): ConceptData =
    existingOrNewConceptData(conceptName.toConceptName(), conceptIdentifier)

fun ConceptDataCollector.existingOrNewConceptData(conceptName: ConceptName, conceptIdentifier: String): ConceptData =
    existingOrNewConceptData(conceptName, conceptIdentifier.toConceptIdentifier())

inline fun <reified C> ConceptDataCollector.existingOrNewConceptData(
    conceptIdentifier: ConceptIdentifier
): ConceptData = existingOrNewConceptData(C::class, conceptIdentifier)

inline fun <reified C> ConceptDataCollector.existingOrNewConceptData(conceptIdentifier: String): ConceptData =
    existingOrNewConceptData(C::class, conceptIdentifier)

fun ConceptDataCollector.newConceptData(conceptName: KClass<*>, conceptIdentifier: ConceptIdentifier): ConceptData =
    newConceptData(conceptName.toConceptName(), conceptIdentifier)

fun ConceptDataCollector.newConceptData(conceptName: KClass<*>, conceptIdentifier: String): ConceptData =
    newConceptData(conceptName.toConceptName(), conceptIdentifier)

fun ConceptDataCollector.newConceptData(conceptName: ConceptName, conceptIdentifier: String): ConceptData =
    newConceptData(conceptName, conceptIdentifier.toConceptIdentifier())

inline fun <reified C> ConceptDataCollector.newConceptData(conceptIdentifier: ConceptIdentifier): ConceptData =
    newConceptData(C::class, conceptIdentifier)

inline fun <reified C> ConceptDataCollector.newConceptData(conceptIdentifier: String): ConceptData =
    newConceptData(C::class, conceptIdentifier)

fun ConceptDataCollector.newConceptData(conceptName: KClass<*>): ConceptData =
    newConceptData(conceptName.toConceptName())

inline fun <reified C> ConceptDataCollector.newConceptData(): ConceptData = newConceptData(C::class)
