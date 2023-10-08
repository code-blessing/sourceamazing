package org.codeblessing.sourceamazing.engine.process.templating

import org.codeblessing.sourceamazing.api.process.templating.TargetFileWithContent
import org.codeblessing.sourceamazing.api.process.templating.TargetFilesCollector
import java.nio.file.Path

class ListTargetFilesCollectorImpl: TargetFilesCollector {
    private val targetFilesWithContent: MutableList<TargetFileWithByteContent> = mutableListOf()
    override fun addFile(targetFile: Path, fileContent: String) {
        addFile(targetFile, fileContent.toByteArray(charset = Charsets.UTF_8))
    }

    override fun addFile(targetFile: Path, fileByteContent: ByteArray) {
        targetFilesWithContent.add(TargetFileWithByteContent(targetFile, fileByteContent))
    }

    fun getTargetFiles(): List<TargetFileWithContent> {
        return targetFilesWithContent
    }
}
