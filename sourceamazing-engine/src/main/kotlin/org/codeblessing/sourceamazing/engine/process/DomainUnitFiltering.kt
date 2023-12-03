package org.codeblessing.sourceamazing.engine.process

import org.codeblessing.sourceamazing.api.process.DomainUnit
import org.codeblessing.sourceamazing.engine.process.DomainUnitName.domainUnitName

object DomainUnitFiltering {

    private const val includeDomainUnitsParamName = "org.codeblessing.sourceamazing.includeDomainUnits"
    private const val includeDomainUnitsDelimiter = ","

    fun filteredDomainUnits(processSession: ProcessSession): List<DomainUnit<*, *>> {
        val isFilteringAtAll = processSession.parameterAccess.hasParameter(includeDomainUnitsParamName)
        val includedDomainUnitNames = if(isFilteringAtAll) {
            processSession.parameterAccess
                .getParameter(includeDomainUnitsParamName)
                .split(includeDomainUnitsDelimiter)
        } else {
            emptyList()
        }
        return processSession.domainUnits
            .filter { filterDomainUnits(it, isFilteringAtAll, includedDomainUnitNames) }
    }


    private fun filterDomainUnits(domainUnit: DomainUnit<*, *>, isFilteringAtAll: Boolean, includedDomainUnitNames: List<String>): Boolean {
        if(!isFilteringAtAll) {
            return true // do not exclude any domain unit
        }
        val domainUnitName = domainUnit.domainUnitName()
        return includedDomainUnitNames.contains(domainUnitName)
    }

}