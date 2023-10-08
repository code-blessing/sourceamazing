package org.codeblessing.sourceamazing.engine.parameters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MultipleSourcesParameterAccessTest {

    @Test
    fun getParameter() {
        // arrange
        val staticParameterSource1 = StaticParameterSource(mapOf(
            "myDirectory" to "/definition/directory/1",
            "myFile" to "/xml/definition/file/1",
            "myFileNotOverwritten" to "/xml/definition/file/1"
        ))

        val staticParameterSource2 = StaticParameterSource(mapOf(
            "myDirectory" to "/definition/directory/2",
            "myFile" to "/xml/definition/file/2",

            ))

        val parameterReader = MultipleSourcesParameterAccess(listOf(staticParameterSource1, staticParameterSource2))

        // act + assert
        assertEquals("/definition/directory/2", parameterReader.getParameter("myDirectory"))
        assertEquals("/xml/definition/file/2", parameterReader.getParameter("myFile"))
        assertEquals("/xml/definition/file/1", parameterReader.getParameter("myFileNotOverwritten"))
    }
}
