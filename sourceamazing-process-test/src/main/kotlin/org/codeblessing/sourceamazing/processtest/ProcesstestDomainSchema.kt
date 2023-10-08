package org.codeblessing.sourceamazing.processtest

import org.codeblessing.sourceamazing.api.process.schema.annotations.ChildConcepts
import org.codeblessing.sourceamazing.api.process.schema.annotations.Schema

@Schema
interface ProcesstestDomainSchema {

    @ChildConcepts(EntityConcept::class)
    fun getEntityConcepts(): List<EntityConcept>
}
