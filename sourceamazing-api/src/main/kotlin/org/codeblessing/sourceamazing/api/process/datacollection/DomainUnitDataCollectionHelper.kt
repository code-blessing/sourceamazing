package org.codeblessing.sourceamazing.api.process.datacollection

import kotlin.reflect.KClass

interface DomainUnitDataCollectionHelper {
    fun <I: Any> createDomainUnitDataCollection(inputDefinitionClass: KClass<I>): DomainUnitDataCollection<I>
}
