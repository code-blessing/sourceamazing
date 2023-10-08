package org.codeblessing.sourceamazing.engine.process.finder

import org.codeblessing.sourceamazing.api.process.DomainUnit
import java.util.*

object DomainUnitFinder {

    fun findAllDomainUnits(): List<DomainUnit<*, *>> {
        val domainUnitServiceLoader: ServiceLoader<DomainUnit<*, *>> = ServiceLoader.load(DomainUnit::class.java)

        return domainUnitServiceLoader.toList()
    }

}
