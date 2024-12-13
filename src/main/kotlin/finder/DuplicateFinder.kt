package finder

import finder.indexing.*
import finder.output.printToFiles
import finder.ui.Ui
import java.nio.file.*
import kotlin.collections.*
import org.apache.commons.cli.*
import java.io.PrintWriter
import kotlin.system.exitProcess
import kotlin.time.measureTimedValue

private const val DOCUMENTATION_URL = "https://flounder.dev/duplicate-finder/"

fun indexAndFind(options: DuplicateFinderOptions): DuplicateFinderReport {
    val (directoryIndex, indexDuration) = measureTimedValue {
        indexDirectory(options)
    }

    val (duplicates, findDuration) = measureTimedValue {
        find(directoryIndex, options)
    }

    return DuplicateFinderReport(
        duplicates,
        indexDuration,
        findDuration
    )
}

fun main(args: Array<String>) {
    val options = Options().apply{
        listOf(
            Option("r", "root", true, "(required) content root path").apply { isRequired = true },
            Option("i", "indexer", true, "indexer (line, file, xml, md, auto), default: auto"),
            Option("o", "output", true, "output file path"),
            Option("v", "verbose", false, "print verbose output"),
            Option("s", "minSimilarity", true, "minimum similarity"),
            Option("l", "minLength", true, "minimum length"),
            Option("d", "minDuplicates", true, "minimum duplicates"),
            Option("f", "fileMask", true, "file mask"),
            Option("h", "headless", false, "run in headless mode"),
            Option("m", "memory", false, "run in low-memory mode"),
            Option("g", "gram", false, "ngram length"),
        ).forEach(::addOption)
    }

    try {
        val cmd = DefaultParser().parse(options, args)
        val defaults = mapOf(
            "output" to "./duplicate_finder_output",
            "minSimilarity" to "0.9",
            "minLength" to "100",
            "minDuplicates" to "1",
            "fileMask" to "",
            "indexer" to "auto",
            "gram" to "3",
        )

        fun cmdOrDefault(name: String) = cmd.getOptionValue(name) ?: defaults[name] ?: error("No default")

        val root = Path.of(cmd.getOptionValue("root"))
        val outputPath = Path.of(cmdOrDefault("output"))
        val minSimilarity = cmdOrDefault("minSimilarity").toDouble()
        val minLength = cmdOrDefault("minLength").toInt()
        val minDuplicates = cmdOrDefault("minDuplicates").toInt()
        val fileMask = cmdOrDefault("fileMask").split(",").filter { it.isNotEmpty() }.toSet()
        val verbose = cmd.hasOption("verbose")
        val headless = cmd.hasOption("headless")
        val lowMemory = cmd.hasOption("memory")
        val indexerOption = cmdOrDefault("indexer")
        val ngramLength = cmdOrDefault("gram").toInt()

        val availableIndexers = setOf("line", "file", "xml", "md", "auto")
        require(indexerOption in availableIndexers) {
            "Invalid indexer: $indexerOption. Allowed values are: $availableIndexers"
        }
        val indexer = when (indexerOption) {
            "line" -> IndexerType.LINE
            "file" -> IndexerType.FILE
            "xml" -> IndexerType.XML
            "md" -> IndexerType.MARKDOWN
            "auto" -> IndexerType.AUTO
            else -> {
                System.err.println("Unsupported indexer: $indexerOption, defaulting to 'auto'")
                IndexerType.AUTO
            }
        }

        val options = DuplicateFinderOptions(
            root,
            minSimilarity,
            minLength,
            minDuplicates,
            fileMask,
            indexer,
            verbose,
            lowMemory,
            ngramLength,
            outputPath
        )

        val report = indexAndFind(options)
        printToFiles(report, options)
        if (!headless) {
            Ui(report, options).show()
        }

    } catch (e: ParseException) {
        println("\n${e.message}\n")
        val formatter = HelpFormatter()
        formatter.printOptions(PrintWriter(System.out, true), 120, options, 0, 5)
        println("\nFor more information, see $DOCUMENTATION_URL\n")
        exitProcess(1)
    }
}
