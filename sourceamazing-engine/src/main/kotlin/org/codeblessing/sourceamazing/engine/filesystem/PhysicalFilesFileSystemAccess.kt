package org.codeblessing.sourceamazing.engine.filesystem

import org.codeblessing.sourceamazing.api.filesystem.FileSystemAccess
import java.io.InputStream
import java.io.Writer
import java.nio.charset.Charset
import java.nio.file.Path

class PhysicalFilesFileSystemAccess: FileSystemAccess {

    override fun classpathResourceAsInputStream(classpathResource: String): InputStream {
        return this.javaClass.getResourceAsStream(classpathResource)
            ?: throw IllegalArgumentException("Resource with name '${classpathResource}' not found.")

    }
    override fun fileAsInputStream(filePath: Path): InputStream {
        FileUtil.checkFileReadable(filePath)
        return filePath.toFile().inputStream()
    }

    override fun createDirectory(directoryPath: Path) {
        directoryPath.toFile().mkdirs()
    }

    override fun writeFile(filePath: Path, fileContent: String) {
        createDirectory(filePath.parent)
        filePath.toFile().writeText(fileContent)
    }

    override fun writeFile(filePath: Path, fileContent: ByteIterator) {
        writeFile(filePath, byteIteratorAsString(fileContent))
    }

    override fun getFileWriter(filePath: Path): Writer {
        return java.io.FileWriter(filePath.toFile())
    }

    override fun close() {
        // nothing to do
    }

    private fun byteIteratorAsString(byteIterator: ByteIterator): String {
        val byteList : MutableList<Byte> = mutableListOf()
        byteIterator.forEach { byte: Byte -> byteList.add(byte) }
        return byteList.toByteArray().toString(Charset.defaultCharset())
    }

}
