package org.codeblessing.sourceamazing.api.filesystem

import java.io.InputStream
import java.io.Writer
import java.nio.file.Path

interface FileSystemAccess {

    fun classpathResourceAsInputStream(classpathResource: String): InputStream
    fun fileAsInputStream(filePath: Path): InputStream
    fun createDirectory(directoryPath: Path)
    fun writeFile(filePath: Path, fileContent: String)
    fun getFileWriter(filePath: Path): Writer
    fun close()
    fun writeFile(filePath: Path, fileContent: ByteIterator)
}
