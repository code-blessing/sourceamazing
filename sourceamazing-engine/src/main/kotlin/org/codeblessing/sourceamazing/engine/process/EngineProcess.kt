package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.engine.process.DomainUnitName.domainUnitName
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptResolver
import org.codeblessing.sourceamazing.engine.process.datacollection.domainunit.DomainUnitDataCollectionHelperImpl
import org.codeblessing.sourceamazing.engine.process.datacollection.validation.exceptions.SchemaValidationException
import org.codeblessing.sourceamazing.engine.process.schema.domainunit.DomainUnitSchemaHelperImpl
import org.codeblessing.sourceamazing.engine.process.templating.domainunit.DomainUnitProcessTargetFilesDataHelperImpl
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class EngineProcess(private val processSession: ProcessSession) {


    fun runProcess() {
        try {
            processSession.loggerFacade.logUserInfo("SourceAmazing started...")
            DomainUnitFiltering.filteredDomainUnits(processSession).forEach { domainUnit -> processDomainUnit(domainUnit) }
        } finally {
            processSession.loggerFacade.logUserInfo("SourceAmazing finished.")
            processSession.loggerFacade.closeLoggerFacade()
        }

    }

    private fun processDomainUnit(domainUnit: DomainUnit<*, *>) {
        val domainUnitName = domainUnit.domainUnitName()
        val loggerFacade = processSession.loggerFacade
        val schema = domainUnit.createSchema(DomainUnitSchemaHelperImpl())
        loggerFacade.logDebug("$domainUnitName: Schema created (${schema.allConcepts().size} concept(s))")
        val conceptData = domainUnit.processDomainUnitInputData(processSession.parameterAccess, DomainUnitDataCollectionHelperImpl(processSession, schema))

        loggerFacade.logDebug("$domainUnitName: Data collected (${conceptData.size} instance(s))")

        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schema, conceptData)

        loggerFacade.logDebug("$domainUnitName: Concepts graph created")


        val targetFilesWithContent = domainUnit.processDomainUnitTargetFiles(
            processSession.parameterAccess,
            DomainUnitProcessTargetFilesDataHelperImpl(conceptGraph)
        )

        loggerFacade.logUserInfo("$domainUnitName: Writing ${targetFilesWithContent.size} file(s)")

        targetFilesWithContent.forEach { targetFileWithContent ->
            loggerFacade.logUserInfo("$domainUnitName: Write file ${targetFileWithContent.targetFile.name}: ${targetFileWithContent.targetFile.normalize().absolutePathString()}")
            processSession.fileSystemAccess.writeFile(targetFileWithContent.targetFile, targetFileWithContent.fileContent.iterator())
        }
    }
}
