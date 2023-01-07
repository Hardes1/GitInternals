package gitinternals

class StubReader : Reader {
    override fun readPath(): String {
        return "/home/hardes1/github/GitSamples/objects/.git"
    }

    override fun readOperation(): String {
        return "tree"
    }

    override fun readObjectHash(): String {
        return "598f75d91162dddf30450fb0d0b56fb09298e6bd"
    }

    override fun readBranch(): String {
        return "second"
    }
}