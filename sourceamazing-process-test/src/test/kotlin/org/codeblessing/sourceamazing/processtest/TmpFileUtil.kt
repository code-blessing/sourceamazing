package org.codeblessing.sourceamazing.processtest

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

object TmpFileUtil {
    // you can choose your build directory during test phase
    private val directory: Path? = null //Paths.get("/Users/me/sourceamazing/tmp")

    fun createTempFile(extension: String = ".txt"): Path {
        return Paths.get(File.createTempFile("tmpfile", extension, directory?.toFile()).toURI())
    }

    fun createTempDirectory(): Path {
        if(directory != null) {
            return directory
        }
        val dummyFile = createTempFile(".dummy")
        return dummyFile.parent
    }

}
