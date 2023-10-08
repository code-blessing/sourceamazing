package org.codeblessing.sourceamazing.engine.process.conceptgraph

import org.codeblessing.sourceamazing.api.process.conceptgraph.exceptions.ParentConceptNotFoundConceptGraphException
import org.codeblessing.sourceamazing.api.process.conceptgraph.exceptions.ReferencedConceptConceptGraphNodeNotFoundException
import org.codeblessing.sourceamazing.api.process.datacollection.exceptions.InvalidConceptParentException
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.engine.process.datacollection.ConceptDataCollector
import org.codeblessing.sourceamazing.engine.process.schema.SchemaCreator
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

private const val databaseTableConceptConst = "DatabaseTable"
private const val databaseFieldConceptConst = "DatabaseField"

private const val tableNameFacetConst = "TableName"
private const val fieldNameFacetConst = "FieldName"
private const val fieldTypeFacetConst = "FieldType"
private const val fieldLengthFacetConst = "FieldLength"
private const val fieldForeignKeyTableFacetConst = "ForeignKeyTable"

class ConceptResolverTest {

    private val databaseTableConceptName = ConceptName.of(databaseTableConceptConst)
    private val databaseTableFieldConceptName = ConceptName.of(databaseFieldConceptConst)

    private val tableNameFacetName = FacetName.of(tableNameFacetConst)
    private val tableFieldNameFacetName = FacetName.of(fieldNameFacetConst)
    private val tableFieldTypeFacetName = FacetName.of(fieldTypeFacetConst)
    private val tableFieldLengthFacetName = FacetName.of(fieldLengthFacetConst)
    private val tableFieldForeignKeyTableFacetName = FacetName.of(fieldForeignKeyTableFacetConst)

    @Schema
    interface DatabaseSchema {

        @ChildConcepts(DatabaseTableConcept::class)
        fun getTables(): List<DatabaseTableConcept>
    }

    @Concept(databaseTableConceptConst)
    interface DatabaseTableConcept {

        @Facet(tableNameFacetConst)
        fun getDatabaseName(): String

        @ChildConcepts(DatabaseFieldConcept::class)
        fun getFields(): List<DatabaseFieldConcept>

    }

    @Concept(databaseFieldConceptConst)
    interface DatabaseFieldConcept {
        @Facet(fieldNameFacetConst)
        fun getFieldName(): String
        @Facet(fieldTypeFacetConst)
        fun getFieldType(): String
        @Facet(fieldLengthFacetConst)
        fun getFieldLength(): Int
        @Facet(fieldForeignKeyTableFacetConst, mandatory = false)
        fun getForeignKeyTable(): DatabaseTableConcept
    }

    private val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(DatabaseSchema::class.java)

    private fun createCollector(schema: SchemaAccess): ConceptDataCollector {
        return ConceptDataCollector(schema)
    }


    @Test
    fun `validate empty concept`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        // act
        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schema, conceptDataCollector.provideConceptData())

        // assert
        assertNotNull(conceptGraph)
    }

    @Test
    fun `validate valid concept data`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")
        val personIdFieldId = ConceptIdentifier.of("PersonIdField")
        val addressTableId = ConceptIdentifier.of("Address")
        val addressIdFieldId = ConceptIdentifier.of("AddressIdField")
        val addressToPersonForeignKeyFieldId = ConceptIdentifier.of("AddressToPersonForeignKeyField")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = personTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "PERSON")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personIdFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "PERSON_ID")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "NUMBER")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)


        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = addressTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "ADDRESS")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = addressIdFieldId,
            parentConceptIdentifier = addressTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "ADDRESS_ID")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "NUMBER")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = addressToPersonForeignKeyFieldId,
            parentConceptIdentifier = addressTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "FK_PERSON_ID")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "NUMBER")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)
            .addOrReplaceFacetValue(tableFieldForeignKeyTableFacetName, personTableId)


        // act
        val conceptGraph = ConceptResolver.validateAndResolveConcepts(schema, conceptDataCollector.provideConceptData())

        // assert
        assertNotNull(conceptGraph)
        conceptGraph.children(databaseTableConceptName)
        assertEquals(2, conceptGraph.children(databaseTableConceptName).size)

        val personTable = conceptGraph.conceptByConceptIdentifier(personTableId)
        assertEquals(personTableId, personTable.conceptIdentifier)
        assertEquals("PERSON", personTable.facetValues[tableNameFacetName])
        assertNull(personTable.parentConceptNode)

        val addressTable = conceptGraph.conceptByConceptIdentifier(addressTableId)
        assertEquals(addressTableId, addressTable.conceptIdentifier)
        assertEquals("ADDRESS", addressTable.facetValues[tableNameFacetName])
        assertNull(addressTable.parentConceptNode)

        val addressToPersonForeignKey = conceptGraph.conceptByConceptIdentifier(addressToPersonForeignKeyFieldId)
        assertEquals(addressToPersonForeignKeyFieldId, addressToPersonForeignKey.conceptIdentifier)
        assertEquals("FK_PERSON_ID", addressToPersonForeignKey.facetValues[tableFieldNameFacetName])
        assertEquals("NUMBER", addressToPersonForeignKey.facetValues[tableFieldTypeFacetName])
        assertEquals(255, addressToPersonForeignKey.facetValues[tableFieldLengthFacetName])
        assertEquals(addressTable, addressToPersonForeignKey.parentConceptNode)

        val referencedPersonTable = addressToPersonForeignKey.facetValues[tableFieldForeignKeyTableFacetName] as ConceptNode
        assertEquals(personTable, referencedPersonTable)

    }

    @Test
    fun `validate invalid concept data with same concept identifier multiple times`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = personTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "PERSON")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personTableId, // here we use the same key again
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "PERSON_ID")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)

        // act + assert
        assertThrows(InvalidConceptParentException::class.java) {
            ConceptResolver.validateAndResolveConcepts(schema, conceptDataCollector.provideConceptData())
        }
    }

    @Test
    fun `validate invalid concept data with unknown parent identifier`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = personTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "PERSON")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = ConceptIdentifier.of("PersonField"),
            parentConceptIdentifier = ConceptIdentifier.of("UnknownParent"),
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "PERSON_ID")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)

        // act + assert
        assertThrows(ParentConceptNotFoundConceptGraphException::class.java) {
            ConceptResolver.validateAndResolveConcepts(schema, conceptDataCollector.provideConceptData())
        }
    }

    @Test
    fun `validate invalid concept data with unknown reference identifier`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")
        val addressTableId = ConceptIdentifier.of("Address")
        val unknownTableId = ConceptIdentifier.of("Unknown")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = personTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "PERSON")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = addressTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "ADDRESS")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = ConceptIdentifier.of("AddressField"),
            parentConceptIdentifier = addressTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "ADDRESS_ID")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "NUMBER")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = ConceptIdentifier.of("PersonForeignKeyField"),
            parentConceptIdentifier = addressTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "FK_PERSON_ID")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "NUMBER")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)
            .addOrReplaceFacetValue(tableFieldForeignKeyTableFacetName, unknownTableId)

        // act + assert
        assertThrows(ReferencedConceptConceptGraphNodeNotFoundException::class.java) {
            ConceptResolver.validateAndResolveConcepts(schema, conceptDataCollector.provideConceptData())
        }
    }

}
