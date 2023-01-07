package gitinternals

class TreeOperation(private val gitPath: String, private val gitCommitHash: String) : Operation {
    override fun perform() {
        val catCommitInfoOperation: ExtendedOperation<CommitFields, MutableList<String>> =
            CatOperation(gitPath, gitCommitHash)
        val commitInfo = catCommitInfoOperation.getCommitInfo()
        val treeHash = commitInfo[CommitFields.TREE] ?: throw IllegalStateException()
        iterate(treeHash.first())
    }

    fun iterate(treeHash: String, prefix: String = "") {
        val catTreeInfoOperation: ExtendedOperation<CommitFields, MutableList<String>> = CatOperation(gitPath, treeHash)
        val info = catTreeInfoOperation.getTreeInfo()
        for (list in info) {
            val name = CatOperation(gitPath, list[1]).readHeader()
            if (name == "blob") {
                println("$prefix${list.last()}")
            } else {
                iterate(list[1], "$prefix${list.last()}/")
            }
        }
    }

    override fun concatenate(): String {
        return "$gitPath/objects/${gitPath.substring(0, 2)}/${gitCommitHash.substring(2)}"
    }
}