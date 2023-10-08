package org.codeblessing.sourceamazing.engine.filesystem

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isReadable

object FileUtil {

    fun checkFileReadable(file: Path) {
        if(!file.exists()) {
            throw IllegalArgumentException("File $file with full path ${file.absolutePathString()} does not exists.")
        }
        if(!file.isReadable()) {
            throw IllegalArgumentException("File $file with full path ${file.absolutePathString()} exists but is not readable.")
        }

    }


}
