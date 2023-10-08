package org.codeblessing.sourceamazing.api.rules

object NameEnforcer {
    private val namePattern = Regex("[A-Z][a-zA-Z0-9]+")
    private val identifierPattern = Regex("[A-Z][a-zA-Z0-9_]+")

    internal fun isValidName(name: String): Boolean {
        return namePattern.matches(name)
    }

    fun isValidNameOrThrow(name: String) {
        if(!isValidName(name)) {
            throw IllegalNameException(
                "The name '$name' is not a valid name. Must start with an uppercase letter, " +
                        "followed by uppercase and lowercase letter and digits. " +
                        "No other characters are allowed. Must be at least two characters long"
            )
        }
    }

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
