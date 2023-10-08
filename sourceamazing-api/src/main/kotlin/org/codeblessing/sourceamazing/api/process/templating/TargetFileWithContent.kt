package org.codeblessing.sourceamazing.api.process.templating

import java.nio.file.Path

interface TargetFileWithContent {
    val targetFile: Path
    val fileContent: ByteArray
}
