package org.codeblessing.sourceamazing.schema.type

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ClassNameUtilTest {

    @Test
    fun simpleNameFromQualifiedName() {
        assertNull(ClassNameUtil.simpleNameFromQualifiedName(null))
        assertEquals("MyClass", ClassNameUtil.simpleNameFromQualifiedName("MyClass"))
        assertEquals("MyClass", ClassNameUtil.simpleNameFromQualifiedName("org.codeblessing.MyClass"))
    }

    @Test
    fun packageFromQualifiedName() {
        assertEquals("", ClassNameUtil.packageFromQualifiedName(null))
        assertEquals("", ClassNameUtil.packageFromQualifiedName("MyClass"))
        assertEquals("org.codeblessing", ClassNameUtil.packageFromQualifiedName("org.codeblessing.MyClass"))
    }
}