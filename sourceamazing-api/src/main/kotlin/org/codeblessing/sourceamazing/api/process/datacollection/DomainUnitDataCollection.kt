package org.codeblessing.sourceamazing.api.process.datacollection

import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess


interface DomainUnitDataCollection<I: Any> {
    fun getDataCollector(): I

    fun getDataCollectionExtensionAccess(): DataCollectionExtensionAccess

    fun getCollectedData(): List<ConceptData>
}
