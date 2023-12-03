package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.process.DomainUnit

object DomainUnitName {

    fun DomainUnit<*, *>.domainUnitName(): String {
        return this.javaClass.simpleName
    }
}