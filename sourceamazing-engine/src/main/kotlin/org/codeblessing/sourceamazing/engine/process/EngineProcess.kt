package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.engine.process.datacollection.DomainUnitDataCollectionHelperImpl
import org.codeblessing.sourceamazing.engine.process.conceptgraph.ConceptResolver
import org.codeblessing.sourceamazing.engine.process.schema.DomainUnitSchemaHelperImpl
import org.codeblessing.sourceamazing.engine.process.templating.DomainUnitProcessTargetFilesDataHelperImpl
import kotlin.io.path.absolutePathString

class EngineProcess(private val processSession: ProcessSession) {



    fun runProcess() {
        processSession.domainUnits.forEach { domainUnit -> processDomainUnit(domainUnit) }
    }

    private fun processDomainUnit(domainUnit: DomainUnit<*, *>) {
        val schema = domainUnit.createSchema(DomainUnitSchemaHelperImpl())
        println("Schema: $schema")
        val conceptData = domainUnit.processDomainUnitInputData(processSession.parameterAccess, DomainUnitDataCollectionHelperImpl(processSession, schema))

        println("InputData: $conceptData")

        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schema, conceptData)

        println("Concepts: $conceptGraph")


        val targetFilesWithContent = domainUnit.processDomainUnitTargetFiles(
            processSession.parameterAccess,
            DomainUnitProcessTargetFilesDataHelperImpl(conceptGraph)
        )

        println("targetFiles: $targetFilesWithContent")

        targetFilesWithContent.forEach { targetFileWithContent ->
            println("File to write: ${targetFileWithContent.targetFile} (${targetFileWithContent.targetFile.absolutePathString()})")
            processSession.fileSystemAccess.writeFile(targetFileWithContent.targetFile, targetFileWithContent.fileContent.iterator())
        }


    }
}
