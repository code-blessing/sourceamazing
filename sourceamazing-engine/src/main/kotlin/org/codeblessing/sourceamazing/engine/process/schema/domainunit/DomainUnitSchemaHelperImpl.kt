package org.codeblessing.sourceamazing.engine.process.schema.domainunit

import org.codeblessing.sourceamazing.api.process.schema.DomainUnitSchemaHelper
import org.codeblessing.sourceamazing.api.process.schema.SchemaAccess
import org.codeblessing.sourceamazing.engine.process.schema.SchemaCreator
import kotlin.reflect.KClass

class DomainUnitSchemaHelperImpl: DomainUnitSchemaHelper {

    override fun <S : Any> createDomainUnitSchema(schemaDefinitionClass: KClass<S>): SchemaAccess {
        return SchemaCreator.createSchemaFromSchemaDefinitionClass(schemaDefinitionClass)
    }

}
