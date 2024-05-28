package org.codeblessing.sourceamazing.schema.api.rules

object NameEnforcer {
    private val identifierPattern = Regex("[A-Z][a-zA-Z0-9_\\-]+")

    internal fun isValidIdentifier(name: String): Boolean {
        return identifierPattern.matches(name)
    }

    fun isValidIdentifierOrThrow(identifier: String) {
        if(!isValidIdentifier(identifier)) {
            throw IllegalNameException(
                "The identifier '$identifier' is not a valid identifier. Must start with an uppercase letter, " +
                        "followed by uppercase and lowercase letter and digits. " +
                        "No other characters are allowed. Must be at least two characters long"
            )
        }
    }

}
