package org.codeblessing.sourceamazing.xmlschema.xsdcreator

import org.codeblessing.sourceamazing.schema.ConceptSchema
import org.codeblessing.sourceamazing.schema.FacetSchema
import org.codeblessing.sourceamazing.schema.FacetType
import org.codeblessing.sourceamazing.schema.SchemaAccess
import org.codeblessing.sourceamazing.xmlschema.XmlNames
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

    private const val conceptIdentifierAttributeGroupName = "conceptIdentifierAttributeGroup"
    private const val conceptIdentifierReferenceAttributeGroupName = "conceptIdentifierReferenceAttributeGroup"

    fun createXsdSchemaContent(schema: SchemaAccess): String {
        val document = initializeDocument()
        val schemaElement = createMainStructure(document)
        attachConceptIdentifierAttribute(document, schemaElement)
        attachXmlRootReferences(document, schemaElement, schema)
        attachAllConceptElements(document, schemaElement, schema)

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

    private fun attachConceptIdentifierAttribute(document: Document, schemaElement: Element) {
        attachComment(document, schemaElement, " CONCEPT IDENTIFIER ATTRIBUTE")
        val conceptIdAttributeGroupElement = createAndAttachXsdElement(document, schemaElement, "attributeGroup")
        setElementXsdAttribute(conceptIdAttributeGroupElement, "name", conceptIdentifierAttributeGroupName)

        val conceptIdAttributeElement = createXsdElement(document, "attribute")
        setElementXsdAttribute(conceptIdAttributeElement, "name", XmlNames.CONCEPT_IDENTIFIER_ATTRIBUTE_NAME)
        setElementXsdAttribute(conceptIdAttributeElement, "type", "$xsdNamespacePrefix:ID")
        conceptIdAttributeGroupElement.appendChild(conceptIdAttributeElement)

        val conceptRefElement = createAndAttachXsdElement(document, schemaElement, "element")
        setElementXsdAttribute(conceptRefElement, "name", XmlNames.CONCEPT_REF_TAG_NAME)
        val conceptRefComplexType = createAndAttachXsdElement(document, conceptRefElement, "complexType")
        val conceptIdRefAttributeElement = createAndAttachXsdElement(document, conceptRefComplexType, "attribute")
        setElementXsdAttribute(conceptIdRefAttributeElement, "name", XmlNames.CONCEPT_IDENTIFIER_REFERENCE_ATTRIBUTE_NAME)
        setElementXsdAttribute(conceptIdRefAttributeElement, "type", "$xsdNamespacePrefix:IDREF")

    }
    private fun attachComment(document: Document, schemaElement: Element, comment: String) {
        schemaElement.appendChild(document.createComment(" - - - - - - - -      $comment     - - - - - - - "))
    }

    private fun attachXmlRootReferences(document: Document, schemaElement: Element, schema: SchemaAccess) {
        attachComment(document, schemaElement, " DEFINITIONS")
        val sourceamazingElement = createAndAttachXsdElement(document, schemaElement, "element")
        setElementXsdAttribute(sourceamazingElement, "name", "sourceamazing")
        val sourceamazingComplexType = createAndAttachXsdElement(document, sourceamazingElement, "complexType")

        val sourceamazingSequence = createAndAttachXsdElement(document, sourceamazingComplexType, "sequence")
        val definitionsElement = createAndAttachXsdElement(document, sourceamazingSequence, "element")
        setElementXsdAttribute(definitionsElement, "name", "definitions")
        val definitionsComplexType = createAndAttachXsdElement(document, definitionsElement, "complexType")
        attachComment(document, definitionsComplexType, " CONCEPTS")
        val definitionsChoice = createAndAttachXsdElement(document, definitionsComplexType, "choice")
        setElementXsdAttribute(definitionsChoice, "minOccurs", "0")
        setElementXsdAttribute(definitionsChoice, "maxOccurs", "unbounded")
        schema.allConcepts().forEach { conceptSchema ->
            val element = createAndAttachXsdElement(document, definitionsChoice, "element")
            setElementXsdAttribute(element, "ref", conceptSchema.toXmlElementTagName())
        }
    }

    private fun attachAllConceptElements(document: Document, schemaElement: Element, schema: SchemaAccess) {
        attachComment(document, schemaElement, " ALL CONCEPTS AS TYPES")
        schema.allConcepts().forEach { conceptSchema ->
            val singletonFacets = conceptSchema.facets.filter { it.maximumOccurrences <=1 }
            val multiValueFacets = conceptSchema.facets.filter { it.maximumOccurrences > 1 }

            attachComment(document, schemaElement, " Concept: ${conceptSchema.toXmlElementTagName()}")

            // tag to create the element for the concept
            val conceptElement = createAndAttachXsdElement(document, schemaElement, "element")
            setElementXsdAttribute(conceptElement, "name", conceptSchema.toXmlElementTagName())
            setElementXsdAttribute(conceptElement, "type", conceptSchema.toXmlElementTypeName())

            // type of the concept tag
            val complexTypeForElement = createAndAttachXsdElement(document, schemaElement, "complexType")
            setElementXsdAttribute(complexTypeForElement, "name", conceptSchema.toXmlElementTypeName())

            // element referencing the multi value facets
            if(multiValueFacets.isNotEmpty()) {
                val all = createAndAttachXsdElement(document, complexTypeForElement, "all")
                multiValueFacets.forEach { facetSchema ->
                    attachComment(document, all, " Facet: ${facetSchema.facetName()}")
                    val elementForFacet = createAndAttachXsdElement(document, all, "element")
                    setElementXsdAttribute(elementForFacet, "name", facetSchema.toXmlElementListTagName())
                    setElementXsdAttribute(elementForFacet, "minOccurs", occurrenceAsString(facetSchema.minimumOccurrences, 1))
                    setElementXsdAttribute(elementForFacet, "maxOccurs", "1")
                    val complexType = createAndAttachXsdElement(document, elementForFacet, "complexType")
                    val sequence = createAndAttachXsdElement(document, complexType, "sequence")
                    setElementXsdAttribute(sequence, "minOccurs", occurrenceAsString(facetSchema.minimumOccurrences))
                    setElementXsdAttribute(sequence, "maxOccurs", occurrenceAsString(facetSchema.maximumOccurrences))

                    when(facetSchema.facetType) {
                        FacetType.TEXT,
                        FacetType.NUMBER,
                        FacetType.BOOLEAN,
                        FacetType.TEXT_ENUMERATION -> {
                            val facetValueElement = createAndAttachXsdElement(document, sequence, "element")
                            setElementXsdAttribute(facetValueElement, "name", XmlNames.FACET_VALUE_TAG_NAME)
                            val valueComplexType = createAndAttachXsdElement(document, facetValueElement, "complexType")
                            val attributeElement = createAndAttachXsdElement(document, valueComplexType, "attribute")
                            setElementXsdAttribute(attributeElement, "name", XmlNames.FACET_SIMPLE_VALUE_ATTRIBUTE_NAME)
                            attachFacetTypeElement(facetSchema, attributeElement, document)
                        }

                        FacetType.REFERENCE -> {
                            val choice = createAndAttachXsdElement(document, sequence, "choice")
                            setElementXsdAttribute(choice, "minOccurs", "1")
                            setElementXsdAttribute(choice, "maxOccurs", "1")
                            facetSchema.referencingConcepts
                                .map { schema.conceptByConceptName(it) }
                                .forEach { referencingConceptSchema ->
                                    val elementForForeignConcept = createAndAttachXsdElement(document, choice, "element")
                                    setElementXsdAttribute(elementForForeignConcept, "name", referencingConceptSchema.toXmlElementTagName())
                                    setElementXsdAttribute(elementForForeignConcept, "type", referencingConceptSchema.toXmlElementTypeName())
                                }
                            val elementForForeignConceptRef = createAndAttachXsdElement(document, choice, "element")
                            setElementXsdAttribute(elementForForeignConceptRef, "ref", XmlNames.CONCEPT_REF_TAG_NAME)
                        }
                    }
                }
            }

            // attribute for the concept identifier
            complexTypeForElement.appendChild(createAttributeReference(document, conceptIdentifierAttributeGroupName))

            // all attributes for the single value facets
            singletonFacets.forEach { facetSchema ->
                val useAttribute = if(facetSchema.minimumOccurrences > 0) "required" else "optional"
                attachComment(document, complexTypeForElement, " Facet: ${facetSchema.facetName()}")
                val attributeElement = createAndAttachXsdElement(document, complexTypeForElement, "attribute")
                setElementXsdAttribute(attributeElement, "name", facetSchema.toXmlAttributeName())
                setElementXsdAttribute(attributeElement, "use", useAttribute)
                attachFacetTypeElement(facetSchema, attributeElement, document)
            }
        }
    }

    private fun attachFacetTypeElement(facetSchema: FacetSchema, attributeElement: Element, document: Document) {
        when(facetSchema.facetType) {
            FacetType.TEXT_ENUMERATION -> {
                val simpleType = createAndAttachXsdElement(document, attributeElement, "simpleType")
                val restriction = createAndAttachXsdElement(document, simpleType, "restriction")
                setElementXsdAttribute(restriction, "base", "$xsdNamespacePrefix:string")
                facetSchema.enumerationValues.forEach { enumerationValue ->
                    val enumerationValueElement = createAndAttachXsdElement(document, restriction, "enumeration")
                    setElementXsdAttribute(enumerationValueElement, XmlNames.FACET_SIMPLE_VALUE_ATTRIBUTE_NAME, enumerationValue)
                }
            }
            FacetType.TEXT -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:string")
            FacetType.NUMBER -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:integer")
            FacetType.BOOLEAN -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:boolean")
            FacetType.REFERENCE -> setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:IDREF")
        }
    }

    private fun ConceptSchema.conceptName(): String {
        return conceptName.simpleName()
    }

    private fun FacetSchema.facetName(): String {
        return facetName.simpleName()
    }


    private fun ConceptSchema.toXmlElementTagName(): String {
        return XmlNames.xmlConceptName(this.conceptName)
    }

    private fun FacetSchema.toXmlAttributeName(): String {
        return XmlNames.xmlFacetName(this.facetName)
    }

    private fun ConceptSchema.toXmlElementRefTagName(): String {
        return "${this.toXmlElementTagName()}Ref"
    }


    private fun FacetSchema.toXmlElementTagName(): String {
        return this.toXmlAttributeName()
    }

    private fun FacetSchema.toXmlElementListTagName(): String {
        return this.toXmlAttributeName()
    }

    private fun ConceptSchema.toXmlElementTypeName(): String {
        return "${this.conceptName()}Type"
    }

    private fun ConceptSchema.toXmlElementRefTypeName(): String {
        return "${this.conceptName()}ReferenceType"
    }

    private fun occurrenceAsString(maximumOccurrences: Int, upperBound: Int = Int.MAX_VALUE): String {
        if(maximumOccurrences == Int.MAX_VALUE) {
            return "unbounded"
        }
        return maximumOccurrences.coerceAtMost(upperBound).toString(10)
    }

    private fun createAttributeReference(document: Document, attributeGroupName: String): Element {
        val attributeElement = createXsdElement(document, "attributeGroup")
        setElementXsdAttribute(attributeElement, "ref", attributeGroupName)
        return attributeElement
    }

    private fun createFixedAttribute(document: Document, attributeName: String, attributeValue: String): Element {
        val attributeElement = createXsdElement(document, "attribute")
        setElementXsdAttribute(attributeElement, "name", attributeName)
        setElementXsdAttribute(attributeElement, "type", "$xsdNamespacePrefix:string")
        setElementXsdAttribute(attributeElement, "fixed", attributeValue)
        return attributeElement
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
}
