package org.codeblessing.sourceamazing.engine.process.templating

import org.codeblessing.sourceamazing.api.process.templating.TargetFileWithContent
import java.nio.file.Path

class TargetFileWithByteContent(override val targetFile: Path, override val fileContent: ByteArray): TargetFileWithContent
