package org.codeblessing.sourceamazing.processtest.formschema

data class ItemId(val id: String) {
    companion object {
        fun of(id: String): ItemId {
            return ItemId(id)
        }
    }
}
