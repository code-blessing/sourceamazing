package org.codeblessing.sourceamazing.api.process.datacollection.extensions

import org.codeblessing.sourceamazing.api.extensions.ExtensionName
import java.nio.file.Path

interface DataCollectionExtensionAccess {

    fun collectWithDataCollectionFromFilesExtension(extensionName: ExtensionName, inputFiles: Set<Path>)

}
