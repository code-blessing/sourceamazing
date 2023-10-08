package org.codeblessing.sourceamazing.engine

import org.codeblessing.sourceamazing.engine.process.EngineProcess
import org.codeblessing.sourceamazing.engine.process.ProcessSession
import org.codeblessing.sourceamazing.engine.process.finder.DomainUnitFinder


fun main() {
    val domainUnits = DomainUnitFinder.findAllDomainUnits()
    val processSession = ProcessSession(domainUnits = domainUnits)

    val process = EngineProcess(processSession)
    process.runProcess()
}
