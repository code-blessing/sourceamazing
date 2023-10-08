package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.api.process.schema.ConceptSchema
import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.datacollection.extensions.ExtensionDataCollector
import org.codeblessing.sourceamazing.api.logger.LoggerFacade
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.tools.CaseUtil
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.ext.DefaultHandler2
import java.nio.file.Path
import java.util.*


class SaxParserHandler(
    private val schema: SchemaAccess,
    private val dataCollector: ExtensionDataCollector,
    private val placeholders: Map<String, String>,
    private val schemaFileDirectory: Path,
    private val fileSystemAccess: FileSystemAccess,
    private val logger: LoggerFacade,
) : DefaultHandler2() {

    private var conceptIdentifierStack: MutableList<ConceptIdentifier> = mutableListOf()
    private var isInDefinitionTag = false
    private val configurations: MutableMap<FacetName, String> = mutableMapOf()

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attr: Attributes) {
        val xmlAttributes = XmlAttribute.attributeList(attr)
        logger.logDebug { "XML: startElement: localName:$localName, attributes: $xmlAttributes" }

        if(isInDefinitionTag) {
            val conceptSchema = getConceptByXmlLocalName(localName) ?: return
            val conceptIdentifier = getConceptIdentifier(xmlAttributes) ?: ConceptIdentifier.random()
            val parentConceptIdentifier = conceptIdentifierStack.lastOrNull()
            val facetValues: Map<FacetName, Any?> = facetValuesFromAttributes(conceptSchema, xmlAttributes)
            try {
                dataCollector
                    .existingOrNewConceptData(conceptSchema.conceptName, conceptIdentifier)
                    .setParentConceptIdentifier(parentConceptIdentifier)
                    .addOrReplaceFacetValues(facetValues)
                this.conceptIdentifierStack.add(conceptIdentifier)
            } catch (ex: Exception) {
                throw SAXException(ex)
            }
            return
        }

        when(localName) {
            "configuration" -> xmlAttributes.forEach { addConfigurationAttribute(it) }
            "definitions" -> isInDefinitionTag = true
        }
    }

    private fun getConceptIdentifier(xmlAttributes: List<XmlAttribute>): ConceptIdentifier? {
        val conceptIdentifierAttribute: XmlAttribute = xmlAttributes.firstOrNull { it.localName == "conceptIdentifier" } ?: return null
        return ConceptIdentifier.of(conceptIdentifierAttribute.value)
    }

    private fun facetValuesFromAttributes(
        conceptSchema: ConceptSchema,
        xmlAttributes: List<XmlAttribute>
    ): Map<FacetName, Any?> {
        val attributeMap: Map<FacetName, XmlAttribute> = xmlAttributes
            .associateBy { FacetName.of(CaseUtil.capitalize(it.localName)) }

        val facetValuesMap: MutableMap<FacetName, Any?> = mutableMapOf()
        conceptSchema.facets.forEach { facetSchema ->
            val schemaFacetName = facetSchema.facetName
            val rawAttributeValue = attributeMap[schemaFacetName]?.value
                ?: configurations[schemaFacetName]
                ?: return@forEach

            val attributeValue = PlaceholderUtil.replacePlaceholders(rawAttributeValue, placeholders)
            val facetValue = XmlFacetValueConverter.convertString(facetSchema, attributeValue)

            facetValuesMap[schemaFacetName] = facetValue
        }

        return facetValuesMap
    }

    private fun addConfigurationAttribute(attribute: XmlAttribute) {
        val facetName = FacetName.of(CaseUtil.capitalize(attribute.localName))
        configurations[facetName] = attribute.value
    }


    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        logger.logDebug { "XML: endElement: localName:$localName" }
        if(localName == "definitions") {
            isInDefinitionTag = false
        }
        if(isConcept(localName)) {
            this.conceptIdentifierStack.removeLast() // pop last element from stack
        }
    }


    private fun isConcept(localName: String): Boolean {
        return getConceptByXmlLocalName(localName) != null
    }

    private fun getConceptByXmlLocalName(localName: String): ConceptSchema? {
        val potentialConceptName = ConceptName.of(CaseUtil.capitalize(localName))
        return if(schema.hasConceptName(potentialConceptName)) {
            schema.conceptByConceptName(potentialConceptName)
        } else {
            null
        }

    }

    override fun fatalError(e: SAXParseException) {
        throw e
    }

    override fun error(e: SAXParseException) {
        throw e
    }

    override fun warning(e: SAXParseException) {
         logger.logWarnings(e.message ?: e.toString())
    }

    override fun resolveEntity(name: String?, publicId: String?, baseURI: String?, systemId: String?): InputSource? {
        logger.logDebug { "XML: resolveEntity: systemId:$systemId, publicId:$publicId, baseURI:$baseURI" }
        return if(systemId != null && systemId.startsWith("./")) {
            InputSource(fileSystemAccess.fileAsInputStream(schemaFileDirectory.resolve(systemId).normalize()))
        } else {
            super.resolveEntity(name, publicId, baseURI, systemId)
        }
    }
}
