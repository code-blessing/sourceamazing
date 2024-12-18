package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.update.DataContext

fun interface BuilderDataProviderInstanceSupplier {

    fun getBuilderDataProviderInstance(dataContext: DataContext): Any
}