package gitinternals

interface Reader {
    fun readPath(): String

    fun readObjectHash(): String

    fun readOperation(): String

    fun readBranch(): String
}