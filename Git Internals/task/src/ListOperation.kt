package gitinternals

import java.io.File
import java.io.FileReader

class ListOperation(private val gitPath: String) : Operation {
    override fun perform() {
        val headName = getHead()
        val path = concatenate()
        val names = mutableListOf<String>()
        File(path).walk().forEach {
            if (!it.isDirectory) {
                names.add(it.name)
            }
        }
        names.sort()
        for (name in names) {
            if (name == headName) {
                println("* $name")
            } else {
                println("  $name")
            }
        }

    }

    private fun getHead(): String {
        val headPath = "$gitPath/HEAD"
        FileReader(headPath).use { file ->
            val lines = file.readLines()[0].split('/')
            return@getHead lines.last()
        }
    }

    override fun concatenate(): String {
        return "$gitPath/refs/heads"
    }
}
