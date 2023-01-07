package gitinternals

import java.io.FileReader

private const val MERGED_COMMIT_PARENTS_COUNT = 2
private const val SINGLE_COMMIT_PARENTS_COUNT = 1

class LogOperation(private val gitPath: String, private val branchName: String) : Operation {


    override fun perform() {
        FileReader(concatenate()).use {
            val initialCommitHash = it.readText().trimEnd('\n')
            iterateOverCommits(initialCommitHash, false)
        }
    }

    private fun iterateOverCommits(gitObjectHash: String, isMerged: Boolean) {
        val catOperation: ExtendedOperation<CommitFields, MutableList<String>> = CatOperation(gitPath, gitObjectHash)
        val info = catOperation.getCommitInfo()
        printCommitInfo(gitObjectHash, info, isMerged)
        val parents = info[CommitFields.PARENTS] ?: throw IllegalStateException()
        if (isMerged || parents.isNotEmpty()) {
            println()
        }
        if (parents.size == MERGED_COMMIT_PARENTS_COUNT && !isMerged) {
            iterateOverCommits(parents.last(), true)
            iterateOverCommits(parents.first(), false)
        } else if (parents.size == SINGLE_COMMIT_PARENTS_COUNT && !isMerged) {
            iterateOverCommits(parents.first(), false)
        }
    }

    private fun printCommitInfo(
        gitObjectHash: String,
        info: LinkedHashMap<CommitFields, MutableList<String>>,
        isMerged: Boolean
    ) {
        println("Commit: $gitObjectHash${if (isMerged) " (merged)" else ""}")
        println("${info[CommitFields.COMMITTER]?.joinToString(" ")}")
        println("${info[CommitFields.COMMIT_MESSAGE]?.joinToString("\n")}")
    }


    override fun concatenate(): String {
        return "$gitPath/refs/heads/$branchName"
    }
}