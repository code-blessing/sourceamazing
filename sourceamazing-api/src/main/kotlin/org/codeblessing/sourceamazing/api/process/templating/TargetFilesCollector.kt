package org.codeblessing.sourceamazing.api.process.templating

import java.nio.file.Path

interface TargetFilesCollector {
    fun addFile(targetFile: Path, fileContent: String)
    fun addFile(targetFile: Path, fileByteContent: ByteArray)
}
