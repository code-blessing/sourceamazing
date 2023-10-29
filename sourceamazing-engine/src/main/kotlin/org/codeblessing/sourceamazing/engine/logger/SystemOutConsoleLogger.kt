package org.codeblessing.sourceamazing.engine.logger

import java.util.logging.*

class SystemOutConsoleLogger: StreamHandler() {

    init {
        setOutputStream(System.out)
    }
    override fun publish(record: LogRecord) {
        super.publish(record)
        flush()
    }

    override fun close() {
        flush()
    }
}