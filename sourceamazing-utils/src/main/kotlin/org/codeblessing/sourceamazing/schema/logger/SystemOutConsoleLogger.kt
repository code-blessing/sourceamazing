package org.codeblessing.sourceamazing.schema.logger

import java.util.logging.LogRecord
import java.util.logging.StreamHandler

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