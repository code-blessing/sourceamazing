package org.codeblessing.sourceamazing.engine.process.datacollection

import org.codeblessing.sourceamazing.api.process.schema.ConceptName
import org.codeblessing.sourceamazing.api.process.schema.FacetName
import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Concept
import org.codeblessing.sourceamazing.api.process.schema.annotations.Facet
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema
import org.codeblessing.sourceamazing.api.process.schema.ConceptIdentifier
import org.codeblessing.sourceamazing.api.process.datacollection.ConceptData
import org.codeblessing.sourceamazing.api.process.datacollection.exceptions.*
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.schema.SchemaCreator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

private const val databaseTableConceptConst = "DatabaseTable"
private const val databaseFieldConceptConst = "DatabaseField"

private const val tableNameFacetConst = "TableName"
private const val fieldNameFacetConst = "FieldName"
private const val fieldTypeFacetConst = "FieldType"
private const val fieldLengthFacetConst = "FieldLength"
private const val foreignKeyFacetConst = "ForeignKey"
class ConceptDataValidatorTest {

    private val databaseTableConceptName = ConceptName.of(databaseTableConceptConst)
    private val databaseTableFieldConceptName = ConceptName.of(databaseFieldConceptConst)

    private val tableNameFacetName = FacetName.of(tableNameFacetConst)
    private val tableFieldNameFacetName = FacetName.of(fieldNameFacetConst)
    private val tableFieldTypeFacetName = FacetName.of(fieldTypeFacetConst)
    private val tableFieldLengthFacetName = FacetName.of(fieldLengthFacetConst)
    private val foreignKeyTableFacetName = FacetName.of(foreignKeyFacetConst)

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

        @Facet(foreignKeyFacetConst, mandatory = false)
        fun getForeignKeyTable(): DatabaseTableConcept?
    }

    private val schema = SchemaCreator.createSchemaFromSchemaDefinitionClass(DatabaseSchema::class.java)
    private fun createCollector(schema: SchemaAccess): ConceptDataCollector {
        return ConceptDataCollector(schema)
    }

    @Test
    fun `validate a valid singe root concept entry`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = personTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "Person")

        // act + assert
        assertForConceptDataValidator(personTableId, conceptDataCollector, schema)
    }

    @Test
    fun `validate a invalid concept entry with unknown concept name`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = ConceptName.of("UnknownConcept"), // unknown concept
            conceptIdentifier = personTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "Person")

        // act + assert
        assertForConceptDataValidator(personTableId, conceptDataCollector, schema, UnknownConceptException::class)
    }


    @Test
    fun `validate a valid child concept entry`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = personTableId,
            parentConceptIdentifier = null,
        ).addOrReplaceFacetValue(tableNameFacetName, "Person")

        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "firstname")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)

        val personNeighborFieldId = ConceptIdentifier.of("Person_neighbor")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personNeighborFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "neighborPersonId")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)
            .addOrReplaceFacetValue(foreignKeyTableFacetName, personTableId)


        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema)
    }


    @Test
    fun `validate a invalid root concept entry with unexpected parent concept`() {
        // arrange
        val conceptDataCollector = createCollector(schema)
        val personTableId = ConceptIdentifier.of("Person")

        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableConceptName,
            conceptIdentifier = personTableId,
            parentConceptIdentifier = ConceptIdentifier.of("InvalidParentIdentifier"), // wrong concept
        ).addOrReplaceFacetValue(tableNameFacetName, "Person")

        // act + assert
        assertForConceptDataValidator(personTableId, conceptDataCollector, schema, InvalidConceptParentException::class)
    }


    @Test
    fun `validate a invalid child concept entry with expected but missing parent concept`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = null, // parent concept missing
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "firstname")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)



        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema, InvalidConceptParentException::class)
    }

    @Test
    fun `validate a concept with missing facet`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        val personTableId = ConceptIdentifier.of("Person")
        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = personTableId,
        )
            //.addOrReplaceFacetValue(tableFieldNameFacetName, "firstname")  // facet tableNameFacetName missing
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)

        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema, MissingFacetValueException::class)
    }

    @Test
    fun `validate a concept with wrong additional facet`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        val personTableId = ConceptIdentifier.of("Person")
        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "firstname")
            .addOrReplaceFacetValue(tableNameFacetName, "foobar") // this facet is not allowed in this concept
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName, 255)

        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema, UnknownFacetNameException::class)
    }

    @Test
    fun `validate a concept with wrong facet type`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        val personTableId = ConceptIdentifier.of("Person")
        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, 23) // type field with wrong type
            .addOrReplaceFacetValue(tableFieldTypeFacetName,  "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName,  255)

        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema, WrongTypeForFacetValueException::class)
    }

    @Test
    fun `validate a concept with mandatory facet type but null value`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        val personTableId = ConceptIdentifier.of("Person")
        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, null) // mandatory type with null value
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName,  255)

        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema, MissingFacetValueException::class)
    }

    @Test
    fun `validate a concept with optional reference facet type and null value`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        val personTableId = ConceptIdentifier.of("Person")
        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "firstname")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName,  255)
            .addOrReplaceFacetValue(foreignKeyTableFacetName, null)

        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema)
    }

    @Test
    fun `validate a concept with reference facet type with value other than ConceptIdentifier`() {
        // arrange
        val conceptDataCollector = createCollector(schema)

        val personTableId = ConceptIdentifier.of("Person")
        val personFirstnameFieldId = ConceptIdentifier.of("Person_firstname")
        conceptDataCollector.existingOrNewConceptData(
            conceptName = databaseTableFieldConceptName,
            conceptIdentifier = personFirstnameFieldId,
            parentConceptIdentifier = personTableId,
        )
            .addOrReplaceFacetValue(tableFieldNameFacetName, "firstname")
            .addOrReplaceFacetValue(tableFieldTypeFacetName, "VARCHAR")
            .addOrReplaceFacetValue(tableFieldLengthFacetName,  255)
            .addOrReplaceFacetValue(foreignKeyTableFacetName, "Person") // not a ConceptIdentifier

        // act + assert
        assertForConceptDataValidator(personFirstnameFieldId, conceptDataCollector, schema, WrongTypeForFacetValueException::class)
    }

    private fun assertForConceptDataValidator(conceptId: ConceptIdentifier, collector: ConceptDataCollector, schema: SchemaAccess, expectedExceptionType: KClass<out Throwable>? = null) {
        val conceptDataToValidate = entryByConceptIdentifier(conceptId, collector)
        if(expectedExceptionType == null) {
            ConceptDataValidator.validateSingleEntry(
                schema = schema,
                conceptData = conceptDataToValidate
            )
        } else {
            assertThrows(expectedExceptionType.java) {
                ConceptDataValidator.validateSingleEntry(
                    schema = schema,
                    conceptData = conceptDataToValidate
                )
            }
        }
    }

    private fun entryByConceptIdentifier(id: ConceptIdentifier, collector: ConceptDataCollector): ConceptData {
        return collector.existingConceptData(id)
    }

}
