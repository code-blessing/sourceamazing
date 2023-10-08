package org.codeblessing.sourceamazing.api.process

import org.codeblessing.sourceamazing.api.process.datacollection.defaults.DefaultConceptDataCollector
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionExtensionAccess
import org.codeblessing.sourceamazing.api.extensions.ExtensionName
import org.codeblessing.sourceamazing.api.parameter.ParameterAccess
import java.nio.file.Paths

abstract class DefaultDomainUnit<S: Any>(schemaDefinitionClass: Class<S>)
    : DomainUnit<S, DefaultConceptDataCollector>(schemaDefinitionClass, DefaultConceptDataCollector::class.java) {
    private val defaultDataCollectionExtensionName = ExtensionName.of("XmlSchemaInputExtension")
    open val defaultXmlPaths = setOf(Paths.get("input-data").resolve("input-data.xml"))

    override fun collectInputData(parameterAccess: ParameterAccess, extensionAccess: DataCollectionExtensionAccess, dataCollector: DefaultConceptDataCollector)
    {
        extensionAccess.collectWithDataCollectionFromFilesExtension(
            extensionName = defaultDataCollectionExtensionName,
            inputFiles = defaultXmlPaths,
        )
    }
}
