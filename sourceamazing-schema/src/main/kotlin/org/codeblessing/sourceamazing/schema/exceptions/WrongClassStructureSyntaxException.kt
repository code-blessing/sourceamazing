package org.codeblessing.sourceamazing.schema.exceptions

import kotlin.reflect.KClass

class WrongClassStructureSyntaxException(clazz: KClass<*>, msg: String) : SyntaxException("$msg (Class: $clazz)")