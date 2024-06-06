package org.codeblessing.sourceamazing.xmlschema

import java.nio.file.Path
import kotlin.io.path.name

object FileDirUtil {

    fun getDirectoryFromFile(file: Path): Path {
        val filename = file.name
        val fileAbsolutePath = file.toAbsolutePath()
        val directory: Path = try {
            fileAbsolutePath.parent
        } catch (ex: RuntimeException) {
            throw IllegalArgumentException("Could not get parent directory of file $filename with absolute path $fileAbsolutePath")
        }
        return directory
    }
}
