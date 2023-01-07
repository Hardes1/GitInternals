package gitinternals

class GitReader : Reader {
    override fun readPath(): String {
        return readln()
    }

    override fun readObjectHash(): String {
        return readln()
    }

    override fun readOperation(): String {
        return readln()
    }

    override fun readBranch(): String {
       return readln()
    }
}