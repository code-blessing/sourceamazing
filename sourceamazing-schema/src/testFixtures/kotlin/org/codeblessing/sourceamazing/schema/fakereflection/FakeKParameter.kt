package org.codeblessing.sourceamazing.schema.fakereflection

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class FakeKParameter(
    override val index: Int,
    override val kind: KParameter.Kind,
    override val name: String?,
    override val type: KType
) : FakeKAnnotatedElement<FakeKParameter>(), KParameter {

    override val isOptional: Boolean
        get() = TODO("Not yet implemented")
    override val isVararg: Boolean
        get() = TODO("Not yet implemented")


}