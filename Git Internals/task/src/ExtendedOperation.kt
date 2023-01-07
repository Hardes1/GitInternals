package gitinternals

interface ExtendedOperation<K, V> : Operation {
    fun getCommitInfo(): LinkedHashMap<K, V>

    fun getTreeInfo(): List<List<String>>

    fun readHeader(): String
}