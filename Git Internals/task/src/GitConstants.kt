package gitinternals

enum class CommitFields(val actualName: String) {
    TREE("tree"),
    PARENTS("parents"),
    AUTHOR("author"),
    COMMITTER("committer"),
    COMMIT_MESSAGE("commit message"),
}