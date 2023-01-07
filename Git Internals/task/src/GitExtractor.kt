package gitinternals


private const val GIT_LOCATION_STRING = "Enter .git directory location:"
private const val SELECT_OPERATION_STRING = "Enter command:"
private const val SELECT_OBJECT_HASH = "Enter git object hash:"
private const val SELECT_COMMIT_HASH = "Enter commit-hash:"
private const val SELECT_BRANCH_NAME = "Enter branch name:"
private const val LIST_OP = "list-branches"
private const val CAT_OP = "cat-file"
private const val LOG_OP = "log"
private const val TREE_OP = "commit-tree"

class GitExtractor(debug: Boolean) {
    private val reader: Reader = if (debug) {
        StubReader()
    } else {
        GitReader()
    }

    fun perform() {
        println(GIT_LOCATION_STRING)
        val gitPath = reader.readPath()
        println(SELECT_OPERATION_STRING)
        val gitOp = reader.readOperation()
        val operation: Operation = when (gitOp) {
            TREE_OP -> {
                println(SELECT_COMMIT_HASH)
                val commitHash = reader.readObjectHash()
                TreeOperation(gitPath, commitHash)
            }
            LOG_OP -> {
                println(SELECT_BRANCH_NAME)
                val branchName = reader.readBranch()
                LogOperation(gitPath, branchName)
            }

            LIST_OP -> {
                ListOperation(gitPath)
            }

            CAT_OP -> {
                println(SELECT_OBJECT_HASH)
                val objectHash = reader.readObjectHash()
                CatOperation(gitPath, objectHash)
            }

            else -> throw IllegalStateException()
        }
        operation.perform()
    }
}