package gitinternals

import java.io.FileInputStream
import java.security.InvalidKeyException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.InflaterInputStream

private const val SPACES_SYMBOL_NUMBER = 32
private const val TERMINATED_SYMBOL_NUMBER = 0
private const val SHA_BYTE_LENGTH = 20
private const val UBYTE_BASE = 256

class CatOperation(private val gitPath: String, private val gitObjectHash: String) :
    ExtendedOperation<CommitFields, MutableList<String>> {
    private val decompressor: InflaterInputStream
    private val byteArray: List<Int>
    private var byteArrayIndex = 0
    private var isHeaderRead = false

    init {
        val inputStream = FileInputStream(concatenate())
        decompressor = InflaterInputStream(inputStream)
        byteArray = decompressor.readAllBytes().map { it.toInt() }
    }

    override fun perform() {
        val header = readHeader()
        println("*${header.uppercase()}*")
        when (header) {
            "commit" -> {
                for ((key, value) in getCommitInfo()) {
                    if (value.isNotEmpty()) {
                        println(
                            "${key.actualName}:${if (key != CommitFields.COMMIT_MESSAGE) " " else ""}${
                                value.joinToString(
                                    prefix = if (key == CommitFields.COMMIT_MESSAGE) "\n" else "",
                                    separator = when (key) {
                                        CommitFields.PARENTS -> " | "
                                        CommitFields.COMMIT_MESSAGE -> "\n"
                                        else -> " "
                                    }
                                )
                            }"
                        )
                    }
                }
            }

            "tree" -> {
                for (line in getTreeInfo()) {
                    println(line.joinToString(" "))
                }
            }

            else -> {
                for (elem in getBlobInfo()) {
                    println(elem)
                }
            }
        }
        decompressor.close()
    }


    override fun readHeader(): String {
        isHeaderRead = true
        var header = ""
        while (byteArrayIndex < byteArray.size) {
            if (byteArray[byteArrayIndex] == SPACES_SYMBOL_NUMBER) {
                break
            }
            header += byteArray[byteArrayIndex++].toChar()
        }
        while (byteArray[byteArrayIndex++] != TERMINATED_SYMBOL_NUMBER);
        return header
    }


    override fun getTreeInfo(): List<List<String>> {
        if (!isHeaderRead) {
            readHeader()
        }
        val treeStrings: MutableList<MutableList<String>> = mutableListOf()
        while (byteArrayIndex < byteArray.size) {
            treeStrings.add(mutableListOf())
            var temporaryIndex = byteArrayIndex
            val currentString = StringBuilder()
            while (byteArray[temporaryIndex] != SPACES_SYMBOL_NUMBER) {
                currentString.append(byteArray[temporaryIndex++].toChar())
            }
            treeStrings.last().add(currentString.toString())
            currentString.clear()
            temporaryIndex++
            while (byteArray[temporaryIndex] != TERMINATED_SYMBOL_NUMBER) {
                currentString.append(byteArray[temporaryIndex++].toChar())
            }
            treeStrings.last().add(currentString.toString())
            currentString.clear()
            temporaryIndex++
            byteArrayIndex = temporaryIndex
            val base = UBYTE_BASE
            while (temporaryIndex < byteArrayIndex + SHA_BYTE_LENGTH) {
                val correctNumber = (base + byteArray[temporaryIndex++]) % base
                currentString.append("%02x".format(correctNumber))
            }
            treeStrings.last().add(1, currentString.toString())
            byteArrayIndex = temporaryIndex
        }
        return treeStrings
    }


    private fun getBlobInfo(): List<String> {
        if (!isHeaderRead) {
            readHeader()
        }
        return byteArray.subList(byteArrayIndex, byteArray.size).map { it.toChar() }.joinToString("").split("\n")
    }

    override fun getCommitInfo(): LinkedHashMap<CommitFields, MutableList<String>> {
        if (!isHeaderRead) {
            readHeader()
        }
        val input = byteArray.subList(byteArrayIndex, byteArray.size).map { it.toChar() }.joinToString("").split("\n")
        val commitInfo = LinkedHashMap<CommitFields, MutableList<String>>()
        for (field in CommitFields.values()) {
            commitInfo[field] = mutableListOf()
        }
        for (line in input) {
            var added = false
            for (field in CommitFields.values()) {
                val splitted = line.split(' ')
                if (field.name.compareTo(splitted[0], ignoreCase = true) == 0) {
                    for (elem in splitted.subList(1, splitted.size)) {
                        commitInfo[field]?.add(elem)
                    }
                    added = true
                } else if (splitted[0] == "parent") {
                    commitInfo[CommitFields.PARENTS]?.add(splitted[1])
                    added = true
                    break
                }
            }
            if (!added && line.isNotEmpty()) {
                commitInfo[CommitFields.COMMIT_MESSAGE]?.add(line)
            }
        }
        val commitFieldsArray = CommitFields.values()
        for (i in 2..3) {
            val array = commitInfo[commitFieldsArray[i]] ?: throw InvalidKeyException()
            for (j in 0 until array.size) {
                array[j] = array[j].trim('<', '>')
            }
            val timezone = array.removeLast()
            val unixtime = array.removeLast()
            val date = Instant.ofEpochSecond(unixtime.toLong()).atZone(ZoneOffset.of(timezone))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"))
            array.add(if (i == 2) "original" else "commit")
            array.add("timestamp:")
            array.add(date)
        }
        return commitInfo
    }

    override fun concatenate(): String {
        return "$gitPath/objects/${gitObjectHash.substring(0, 2)}/${gitObjectHash.substring(2)}"
    }


}