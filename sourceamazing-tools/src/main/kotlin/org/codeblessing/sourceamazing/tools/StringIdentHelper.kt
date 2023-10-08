package org.codeblessing.sourceamazing.tools

object StringIdentHelper {

    const val marker = "{nestedIdent}"

    fun String.identForMarker(): String {
        return insertIdentForMarker(this)
    }
    fun insertIdentForMarker(text: String): String {
        val linesWithIdent = mutableListOf<String>()

        var isInNestedIdentBlock = false
        var additionalIdent = ""
        text.lines().forEach { oldLine ->
            val additionalIdentForLine = additionalIdent
            var lineIndex = 0
            var lineWithIdent = ""
            while (lineIndex < oldLine.length) {
                val nestedIdentIndex = oldLine.indexOf(marker, lineIndex)
                if(hasNestedIdentMarker(nestedIdentIndex)) {
                    if(isInNestedIdentBlock) {
                        additionalIdent = ""
                        isInNestedIdentBlock = false
                    } else {
                        additionalIdent = oldLine.substring(0, nestedIdentIndex)
                        isInNestedIdentBlock = true
                    }

                    lineWithIdent += oldLine.substring(lineIndex, nestedIdentIndex)
                    lineIndex = nestedIdentIndex + marker.length
                } else {
                    lineWithIdent += oldLine.substring(lineIndex, oldLine.length)
                    lineIndex = oldLine.length
                }
            }

            linesWithIdent.add(additionalIdentForLine + lineWithIdent)
        }

        return linesWithIdent.joinToString("\n").trimIndent()
    }

    private fun hasNestedIdentMarker(index: Int): Boolean {
        return index > -1
    }

}
