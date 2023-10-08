package org.codeblessing.sourceamazing.engine.process.schema

import org.codeblessing.sourceamazing.api.process.schema.DomainUnitSchemaHelper
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess

class DomainUnitSchemaHelperImpl(): DomainUnitSchemaHelper {

    override fun <S : Any> createDomainUnitSchema(schemaDefinitionClass: Class<S>): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaDefinitionClass)
    }

}
