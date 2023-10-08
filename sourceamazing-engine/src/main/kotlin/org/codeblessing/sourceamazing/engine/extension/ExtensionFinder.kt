package org.codeblessing.sourceamazing.engine.extension

import org.codeblessing.sourceamazing.api.process.datacollection.extensions.DataCollectionFromFilesExtension
import java.util.*

object ExtensionFinder {

    fun findAllDataCollectionFromFilesExtensions(): List<DataCollectionFromFilesExtension> {
        val extensionServiceLoader: ServiceLoader<DataCollectionFromFilesExtension> = ServiceLoader.load(
            DataCollectionFromFilesExtension::class.java)

        return extensionServiceLoader.toList()
    }

}
