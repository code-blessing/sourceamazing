package org.codeblessing.sourceamazing.schema.type

import org.codeblessing.sourceamazing.schema.api.annotations.Schema
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AnnotationExtensionsTest {

    @Schema(concepts = [])
    private interface ClassWithSourceamazingAnnotation

    @Test
    fun `test with source amazing annotation`() {
        val annotationFromSourceamazing = ClassWithSourceamazingAnnotation::class.annotations.first()
        assertTrue(annotationFromSourceamazing.isAnnotationFromSourceAmazing())
    }



    @Deprecated("Only an annotation not from source amazing")
    private interface ClassWithoutSourceamazingAnnotation

    @Test
    fun `test with another annotation than source amazing annotation`() {
        @Suppress("DEPRECATION")
        val annotationNotFromSourceamazing = ClassWithoutSourceamazingAnnotation::class.annotations.first()

        assertFalse(annotationNotFromSourceamazing.isAnnotationFromSourceAmazing())
    }

}