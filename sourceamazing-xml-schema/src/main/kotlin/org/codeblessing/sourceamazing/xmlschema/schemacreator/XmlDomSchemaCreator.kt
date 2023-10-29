package org.codeblessing.sourceamazing.xmlschema.schemacreator

import org.codeblessing.sourceamazing.api.process.schema.*
import org.codeblessing.sourceamazing.tools.CaseUtil
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


object XmlDomSchemaCreator {
    private const val xsdNamespace = "http://www.w3.org/2001/XMLSchema"
    private const val xsdNamespacePrefix = "xsd"

    fun createXsdSchemaContent(schema: SchemaAccess): String {
        val document = initializeDocument()
        val schemaElement = createMainStructure(document)
        attachConceptIdentifierAttribute(document, schemaElement, schema)
        attachRootConceptReferences(document, schemaElement, schema)
        attachAllConceptElements(document, schemaElement, schema)
        attachAllConceptAttributes(document, schemaElement, schema)
        //attachConfigurationElement(document, schemaElement, schema)

        return transformDocumentToString(document)
    }

    private fun createMainStructure(document: Document): Element {
        val overallXmlSchemaName = "sourceamazing-xml-schema"
        val schemaElement: Element = document.createElementNS(xsdNamespace, xsdName("schema"))

        schemaElement.setAttribute("targetNamespace", "https://codeblessing.org/sourceamazing/$overallXmlSchemaName")
        schemaElement.setAttribute("xmlns", "https://codeblessing.org/sourceamazing/$overallXmlSchemaName")
        schemaElement.setAttribute( "elementFormDefault", "qualified")

        document.appendChild(schemaElement)

        return schemaElement
    }

    private fun attachConceptIdentifierAttribute(document: Document, schemaElement: Element, schema: SchemaAccess) {
        attachComment(document, schemaElement, " CONCEPT IDENTIFIER ATTRIBUTE")
        val attributeGroupElement = createAndAttachXsdElement(document, schemaElement, "attributeGroup")
        setElementXsdAttribute(attributeGroupElement, "name", "conceptIdentifier")

        val attributeElement = createXsdElement(document, "attribute")
        setElementXsdAttribute(attributeElement, "name", "conceptIdentifier")
        setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:ID")

        attributeGroupElement.appendChild(attributeElement)
    }
    private fun attachComment(document: Document, schemaElement: Element, comment: String) {
        schemaElement.appendChild(document.createComment(" - - - - - - - -      $comment     - - - - - - - "))
    }

    private fun attachConfigurationElement(document: Document, schemaElement: Element, schema: SchemaAccess) {
        attachComment(document, schemaElement, " CONFIGURATION ELEMENT")
        val complexType = createAndAttachXsdElement(document, schemaElement, "complexType")
        setElementXsdAttribute(complexType, "name", "configurationType")
        schema.allConcepts().forEach { conceptSchema ->
            conceptSchema.facets.forEach { facetSchema ->
                    complexType.appendChild(
                        createFacetAttributeReference(
                        document,
                        conceptSchema.conceptName,
                        facetSchema.facetName
                    )
                    )
                }
        }
    }

    private fun attachRootConceptReferences(document: Document, schemaElement: Element, schema: SchemaAccess) {
        attachComment(document, schemaElement, " CONFIGURATION AND DEFINITIONS")
        val sourceamazingElement = createAndAttachXsdElement(document, schemaElement, "element")
        setElementXsdAttribute(sourceamazingElement, "name", "sourceamazing")
        val sourceamazingComplexType = createAndAttachXsdElement(document, sourceamazingElement, "complexType")

        val sourceamazingSequence = createAndAttachXsdElement(document, sourceamazingComplexType, "sequence")
        setElementXsdAttribute(sourceamazingSequence, "minOccurs", "1")
        setElementXsdAttribute(sourceamazingSequence, "maxOccurs", "1")
        val definitionsElement = createAndAttachXsdElement(document, sourceamazingSequence, "element")
        setElementXsdAttribute(definitionsElement, "name", "definitions")
        val definitionsComplexType = createAndAttachXsdElement(document, definitionsElement, "complexType")
        attachComment(document, definitionsComplexType, " ROOT CONCEPTS")
        val definitionsChoice = createAndAttachXsdElement(document, definitionsComplexType, "choice")
        setElementXsdAttribute(definitionsChoice, "minOccurs", "0")
        setElementXsdAttribute(definitionsChoice, "maxOccurs", "unbounded")
        schema.allRootConcepts().forEach { conceptSchema ->
            val conceptXmlSchemaName = conceptSchema.toXmlElementTagName()
            val element = createAndAttachXsdElement(document, definitionsChoice, "element")
            setElementXsdAttribute(element, "name", conceptXmlSchemaName)
            setElementXsdAttribute(element, "type", "${conceptXmlSchemaName}Type")
        }
    }

    private fun attachAllConceptElements(document: Document, schemaElement: Element, schema: SchemaAccess) {
        attachComment(document, schemaElement, " ALL CONCEPTS AS TYPES")
        schema.allConcepts().forEach { conceptSchema ->
            val conceptXmlSchemaName = conceptSchema.toXmlElementTagName()
            val complexType = createAndAttachXsdElement(document, schemaElement, "complexType")
            setElementXsdAttribute(complexType, "name", "${conceptXmlSchemaName}Type")
            val choice = createAndAttachXsdElement(document, complexType, "choice")
            setElementXsdAttribute(choice, "minOccurs", "0")
            setElementXsdAttribute(choice, "maxOccurs", "unbounded")
            schema.allChildrenConcepts(conceptSchema).forEach { childConceptNode ->
                val enclosedConceptXmlSchemaName = childConceptNode.toXmlElementTagName()
                val elementRef = createAndAttachXsdElement(document, choice, "element")
                setElementXsdAttribute(elementRef, "name", enclosedConceptXmlSchemaName)
                setElementXsdAttribute(elementRef, "type", "${enclosedConceptXmlSchemaName}Type")
            }
            complexType.appendChild(createAttributeReference(document, "conceptIdentifier"))
            conceptSchema.facets.forEach { facetSchema ->
                    complexType.appendChild(createFacetAttributeReference(document, conceptSchema.conceptName, facetSchema.facetName))
                }
        }
    }

    private fun attachAllConceptAttributes(document: Document, schemaElement: Element, schema: SchemaAccess) {
        attachComment(document, schemaElement, " ALL ATTRIBUTES ")
        schema.allConcepts().forEach { conceptNode ->
            conceptNode.facets
                .forEach { facetSchema ->
                    schemaElement.appendChild(createFacetAttributeElement(document, conceptNode.conceptName, facetSchema.facetName, facetSchema))
                }
        }
    }

    private fun createFacetAttributeElement(
        document: Document,
        conceptName: ConceptName,
        facetName: FacetName,
        facetSchema: FacetSchema
    ): Element {
        val attributeGroupElement = createXsdElement(document, "attributeGroup")
        setElementXsdAttribute(attributeGroupElement, "name", facetName.toXmlAttributeReferenceName(conceptName))

        val attributeElement = createXsdElement(document, "attribute")
        setElementXsdAttribute(attributeElement, "name", facetName.toXmlAttributeName())

        when(facetSchema.facetType) {
            FacetTypeEnum.TEXT_ENUMERATION -> {
                val simpleType = createAndAttachXsdElement(document, attributeElement, "simpleType")
                val restriction = createAndAttachXsdElement(document, simpleType, "restriction")
                setElementXsdAttribute(restriction, "base", "$xsdNamespacePrefix:string")
                facetSchema.enumerationValues().forEach { enumerationValue ->
                    val enumerationValueElement = createAndAttachXsdElement(document, restriction, "enumeration")
                    setElementXsdAttribute(enumerationValueElement, "value", enumerationValue.name)
                }
            }
            FacetTypeEnum.TEXT -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:string")
            FacetTypeEnum.NUMBER -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:integer")
            FacetTypeEnum.BOOLEAN -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:boolean")
            FacetTypeEnum.REFERENCE -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:IDREF") // TODO choose a better type
        }

        attributeGroupElement.appendChild(attributeElement)
        return attributeGroupElement
    }

    private fun createAttributeReference(document: Document, attributeGroupName: String): Element {
        val attributeElement = createXsdElement(document, "attributeGroup")
        setElementXsdAttribute(attributeElement, "ref", attributeGroupName)
        return attributeElement
    }

    private fun createFacetAttributeReference(document: Document, conceptName: ConceptName, facetName: FacetName): Element {
        return createAttributeReference(document, facetName.toXmlAttributeReferenceName(conceptName))
    }

    private fun initializeDocument(): Document {
        val docFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder: DocumentBuilder = docFactory.newDocumentBuilder()
        return docBuilder.newDocument()
    }

    private fun transformDocumentToString(doc: Document): String {
        val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
        transformerFactory.setAttribute("indent-number", 4);
        val transformer: Transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes") // pretty print XML
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")

        val domSource = DOMSource(doc)

        StringWriter().use { writer ->
            val result = StreamResult(writer)
            transformer.transform(domSource, result)
            return@transformDocumentToString writer.toString()
        }
    }

    private fun ConceptSchema.toXmlElementTagName(): String {
        return CaseUtil.decapitalize(this.conceptName.name)
    }

//    private fun schemaAttributeType(facet: Facet): String {
//        return when(facet) {
//            is StringFacet -> "$xsdNamespacePrefix:string"
//            // IntegerNumberFacetType -> "$xsdNamespacePrefix:integer"
//            is BooleanFacet -> "$xsdNamespacePrefix:boolean"
//            // DirectoryFacetType -> "$xsdNamespacePrefix:string"
//            // FileFacetType -> "$xsdNamespacePrefix:string"
//            else -> throw IllegalArgumentException("FacetType is not supported: $facet")
//        }
//    }

    private fun xsdName(attributeName:String): String {
        return "$xsdNamespacePrefix:$attributeName"
    }

    private fun createXsdElement(document: Document, elementName: String): Element {
        return document.createElementNS(xsdNamespace, xsdName(elementName))
    }

    private fun createAndAttachXsdElement(document: Document, parentElement: Element, elementName: String): Element {
        val element = createXsdElement(document, elementName)
        parentElement.appendChild(element)
        return element
    }

    private fun setElementXsdAttribute(element: Element, attributeName: String, attributeValue: String) {
        element.setAttribute(attributeName, attributeValue)
    }

    private fun FacetName.toXmlAttributeName(): String {
        return CaseUtil.decapitalize(this.name)
    }

    private fun FacetName.toXmlAttributeReferenceName(conceptName: ConceptName): String {
        return "${CaseUtil.decapitalize(conceptName.name)}${this.name}"
    }


}
