package org.codeblessing.sourceamazing.xmlschema.parser


object PlaceholderUtil {

    private val regexPattern = Regex("[@][{](\\w+)}")

    fun replacePlaceholders(templateString: String, placeholders: Map<String, String>): String {
        val resolvedTemplateParts: MutableList<String> = mutableListOf()
        var indexInTemplateString = 0

        regexPattern.findAll(templateString).forEach { matchResult ->
            val entirePlaceholderGroup = matchResult.groups.first()
                ?: throw IllegalStateException("Found a regex match but no group within for template $templateString: ${matchResult.groups}")
            val key = matchResult.groupValues.last()
            val replacementValue = placeholders[key] ?: throw IllegalArgumentException("No placeholder value found for placeholder $key")

            val rangeBeforePlaceholder = indexInTemplateString until entirePlaceholderGroup.range.first
            resolvedTemplateParts.add(templateString.substring(rangeBeforePlaceholder))
            resolvedTemplateParts.add(replacementValue)

            indexInTemplateString = entirePlaceholderGroup.range.last + 1
        }

        resolvedTemplateParts.add(templateString.substring(indexInTemplateString))

        return resolvedTemplateParts.joinToString("")
    }
}
