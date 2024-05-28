package org.codeblessing.sourceamazing.xmlschema.parser

import org.codeblessing.sourceamazing.schema.*
import org.codeblessing.sourceamazing.schema.api.*
import org.codeblessing.sourceamazing.schema.filesystem.FileSystemAccess
import org.codeblessing.sourceamazing.schema.logger.LoggerFacade
import org.codeblessing.sourceamazing.schema.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.schema.util.ConceptIdentifierUtil
import org.codeblessing.sourceamazing.xmlschema.XmlNames
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.ext.DefaultHandler2
import java.nio.file.Path
import java.util.*


class SaxParserHandler(
    private val schema: SchemaAccess,
    private val dataCollector: ConceptDataCollector,
    private val placeholders: Map<String, String>,
    private val schemaFileDirectory: Path,
    private val fileSystemAccess: FileSystemAccess,
    private val logger: LoggerFacade,
) : DefaultHandler2() {

    private var conceptStack: MutableList<XmlStackElement> = mutableListOf()
    private var isInDefinitionTag = false

    data class XmlStackElement(
        val conceptData: ConceptData,
        val conceptSchema: ConceptSchema,
        val currentFacetSchema: FacetSchema?,
    )

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attr: Attributes) {
        val xmlAttributes = XmlAttribute.attributeList(attr)
        logger.logDebug { "XML: startElement: localName:$localName, attributes: $xmlAttributes" }

        if(isInDefinitionTag) {

            try {
                this.conceptStack.add(handleXmlTag(localName, xmlAttributes))
            } catch (ex: Exception) {
                throw SAXException(ex)
            }
            return
        }

        when(localName) {
            "definitions" -> isInDefinitionTag = true
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        logger.logDebug { "XML: endElement: localName:$localName" }
        if(localName == "definitions") {
            isInDefinitionTag = false
        }

        if(isInDefinitionTag){
            this.conceptStack.removeLast() // pop last element from stack
        }
    }

    private fun handleXmlTag(localName: String, xmlAttributes: List<XmlAttribute>): XmlStackElement {
        val currentStackElement = getCurrentXmlStackElement()
        val conceptSchemaFromTagName = XmlNames.conceptFromXmlConceptName(localName, schema)
        if(conceptSchemaFromTagName != null) {
            return declareNewConcept(conceptSchemaFromTagName, xmlAttributes)
        }

        if(currentStackElement == null) {
            throw IllegalStateException("No stack element. Probably not valid concept name '$localName'.")
        }
        val facetSchemaFromTagName = XmlNames.facetFromXmlFacetName(localName, currentStackElement.conceptSchema)
        if(facetSchemaFromTagName != null) {
            return declareNewFacet(facetSchemaFromTagName)
        }

        if(localName == XmlNames.CONCEPT_REF_TAG_NAME) {
            return addFacetReferenceValue(xmlAttributes)
        }

        if(localName == XmlNames.FACET_VALUE_TAG_NAME) {
            return addFacetValue(xmlAttributes)
        }

        throw IllegalStateException("Unsupported tag '$localName'.")
    }

    private fun declareNewConcept(newConceptSchema: ConceptSchema, xmlAttributes: List<XmlAttribute>): XmlStackElement {
        val newConceptIdentifier = extractXmlAttributeValue(XmlNames.CONCEPT_IDENTIFIER_ATTRIBUTE_NAME, xmlAttributes)
            ?.let { ConceptIdentifier.of(it) }
            ?: ConceptIdentifierUtil.random(newConceptSchema.conceptName)
        if(getCurrentXmlStackElement() != null) {
            addRawValueToCurrentFacet(newConceptIdentifier.name)
        }

        val newConceptData = dataCollector
            .existingOrNewConceptData(newConceptSchema.conceptName, newConceptIdentifier)
        val facetValues: Map<FacetName, Any> = facetValuesFromAttributes(newConceptSchema, xmlAttributes)
        facetValues.forEach { (facetName, value) ->
            newConceptData.addFacetValue(facetName, value)
        }

        return XmlStackElement(
            conceptData = newConceptData,
            conceptSchema = newConceptSchema,
            currentFacetSchema = null,
        )

    }
    private fun declareNewFacet(facetSchema: FacetSchema): XmlStackElement {
        val currentConceptData = this.getCurrentXmlStackElement()
            ?: throw IllegalStateException("Facet declaration without a concept class.")
        return currentConceptData.copy(currentFacetSchema = facetSchema)
    }

    private fun addFacetValue(xmlAttributes: List<XmlAttribute>): XmlStackElement {
        return addAttributeFacetValue(XmlNames.FACET_SIMPLE_VALUE_ATTRIBUTE_NAME, xmlAttributes)
    }

    private fun addFacetReferenceValue(xmlAttributes: List<XmlAttribute>): XmlStackElement {
        return addAttributeFacetValue(XmlNames.CONCEPT_IDENTIFIER_REFERENCE_ATTRIBUTE_NAME, xmlAttributes)
    }

    private fun addAttributeFacetValue(attributeName: String, xmlAttributes: List<XmlAttribute>): XmlStackElement {
        val value = extractXmlAttributeValue(attributeName, xmlAttributes)
            ?: throw IllegalStateException("No value for attribute '${attributeName}' in $xmlAttributes.")
        return addRawValueToCurrentFacet(value).copy()
    }

    private fun addRawValueToCurrentFacet(rawValue: String): XmlStackElement {
        val xmlStackElement = this.getCurrentXmlStackElement()
            ?: throw IllegalStateException("No current stack element found to add value.")
        val currentFacetSchema = xmlStackElement.currentFacetSchema
            ?: throw IllegalStateException("Can not add value as no facet defined.")

        val value = XmlFacetValueConverter.convertString(currentFacetSchema, rawValue)
        xmlStackElement.conceptData.addFacetValue(currentFacetSchema.facetName, value)
        return xmlStackElement
    }

    private fun facetValuesFromAttributes(
        conceptSchema: ConceptSchema,
        xmlAttributes: List<XmlAttribute>
    ): Map<FacetName, Any> {
        val facetValuesMap: MutableMap<FacetName, Any> = mutableMapOf()
        xmlAttributes.forEach { xmlAttribute ->
            val attributeName = xmlAttribute.qName
            val facetSchema = XmlNames.facetFromXmlFacetName(attributeName, conceptSchema)
            if(facetSchema != null) {
                val attributeValue = PlaceholderUtil.replacePlaceholders(xmlAttribute.value, placeholders)
                val facetValue = XmlFacetValueConverter.convertString(facetSchema, attributeValue)
                facetValuesMap[facetSchema.facetName] = facetValue
            } else {
                logger.logDebug("Unknown facet '${xmlAttribute.localName}' in concept '${conceptSchema.conceptName}': $xmlAttributes")
            }
        }
        return facetValuesMap
    }


    private fun getCurrentXmlStackElement(): XmlStackElement? {
        return this.conceptStack.lastOrNull()
    }

    private fun extractXmlAttributeValue(attributeName: String, xmlAttributes: List<XmlAttribute>): String? {
        return xmlAttributes
            .filter { it.localName == attributeName }
            .map { it.value }
            .firstOrNull()
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
