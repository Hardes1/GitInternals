package gitinternals

fun main(args: Array<String>) {
    val debug = args.size > 0 && args[0] == "-d"
    val gitExtractor = GitExtractor(debug)
    gitExtractor.perform()
}