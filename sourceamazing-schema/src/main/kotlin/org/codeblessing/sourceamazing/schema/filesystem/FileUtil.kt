package org.codeblessing.sourceamazing.schema.filesystem

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isReadable
import kotlin.io.path.isWritable

object FileUtil {

    fun checkFileReadable(file: Path) {
        if(!file.exists()) {
            throw IllegalArgumentException("File $file with full path ${file.absolutePathString()} does not exists.")
        }
        if(!file.isReadable()) {
            throw IllegalArgumentException("File $file with full path ${file.absolutePathString()} exists but is not readable.")
        }
    }

    fun checkFileWritable(file: Path) {
        if(!file.isWritable()) {
            throw IllegalArgumentException("File $file with full path ${file.absolutePathString()} is not writable.")
        }
    }

}
