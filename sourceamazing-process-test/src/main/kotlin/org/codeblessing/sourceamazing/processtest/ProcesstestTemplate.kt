package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.tools.StringIdentHelper.identForMarker
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object ProcesstestTemplate {

    private const val ident = "  "

    fun createExampleTemplate(targetFile: Path, entity: EntityConcept): String {

        val entityAttributes = entity.getEntityAttributes()
            .joinToString("\n") { createEntityAttributeSubTemplate(it) }

        return """
            Filename: ${targetFile.absolutePathString()}
            ---------
            
            Entity name: ${entity.entityName()}
            Entity alternative name: ${entity.entityAlternativeName()}
            
            Entity attributes:
            {nestedIdent}$entityAttributes{nestedIdent} 
            
        """.identForMarker()
    }

    private fun createEntityAttributeSubTemplate(entityAttribute: EntityAttributeConcept): String {
        return """
            Entity Attribute name: ${entityAttribute.attributeName()}
        """.replaceIndent(ident)
    }

    fun createExampleIndexTemplate(targetIndexFile: Path, entities: List<EntityConcept>): String {
        val entityList = entities
            .joinToString("\n") { it.entityName() }

        return """
            Filename: ${targetIndexFile.absolutePathString()}
            ---------
            
            {nestedIdent}$entityList{nestedIdent} 
            
        """.identForMarker()
    }

}
