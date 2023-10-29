package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.engine.process.datacollection.DomainUnitDataCollectionHelperImpl
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptResolver
import org.codeblessing.sourceamazing.engine.process.schema.DomainUnitSchemaHelperImpl
import org.codeblessing.sourceamazing.engine.process.templating.DomainUnitProcessTargetFilesDataHelperImpl
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class EngineProcess(private val processSession: ProcessSession) {



    fun runProcess() {
        processSession.loggerFacade.logUserInfo("SourceAmazing started...")
        processSession.domainUnits.forEach { domainUnit -> processDomainUnit(domainUnit) }
        processSession.loggerFacade.logUserInfo("SourceAmazing finished.")
        processSession.loggerFacade.closeLoggerFacade()
    }

    private fun processDomainUnit(domainUnit: DomainUnit<*, *>) {
        val domainUnitDescription = domainUnit.javaClass.simpleName
        val loggerFacade = processSession.loggerFacade
        val schema = domainUnit.createSchema(DomainUnitSchemaHelperImpl())
        loggerFacade.logDebug("$domainUnitDescription: Schema created (${schema.allRootConcepts().size} concept(s))")
        val conceptData = domainUnit.processDomainUnitInputData(processSession.parameterAccess, DomainUnitDataCollectionHelperImpl(processSession, schema))

        loggerFacade.logDebug("$domainUnitDescription: Data collected (${conceptData.size} instance(s))")

        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schema, conceptData)

        loggerFacade.logDebug("$domainUnitDescription: Concepts graph created")


        val targetFilesWithContent = domainUnit.processDomainUnitTargetFiles(
            processSession.parameterAccess,
            DomainUnitProcessTargetFilesDataHelperImpl(conceptGraph)
        )

        loggerFacade.logUserInfo("$domainUnitDescription: Writing ${targetFilesWithContent.size} file(s)")

        targetFilesWithContent.forEach { targetFileWithContent ->
            loggerFacade.logUserInfo("$domainUnitDescription: Write file ${targetFileWithContent.targetFile.name}: ${targetFileWithContent.targetFile.normalize().absolutePathString()}")
            processSession.fileSystemAccess.writeFile(targetFileWithContent.targetFile, targetFileWithContent.fileContent.iterator())
        }


    }
}
