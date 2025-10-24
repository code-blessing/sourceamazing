package org.codeblessing.sourceamazing.schema.api.datacollection

import org.codeblessing.sourceamazing.schema.api.ClazzAndModelId

interface ClazzModel {
    val clazzAndModelId: ClazzAndModelId

    fun replaceWithClazzPropertyValues(clazzProperty: String, values: List<Any>): ClazzModel

    fun replaceWithClazzPropertyReferences(clazzProperty: String, references: List<Any>): ClazzModel

    fun addClazzPropertyValue(clazzProperty: String, value: Any): ClazzModel

    fun addClazzPropertyReference(clazzProperty: String, references: Any): ClazzModel

    fun addClazzPropertyValues(clazzProperty: String, values: List<Any>): ClazzModel

    fun addClazzPropertyReferences(clazzProperty: String, references: List<Any>): ClazzModel
}
