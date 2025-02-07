package finder

import finder.output.printToFiles
import finder.indexing.Index
import finder.parsing.ParserType
import finder.ui.Ui
import java.nio.file.*
import kotlin.collections.*
import org.apache.commons.cli.*
import java.io.PrintWriter
import kotlin.system.exitProcess
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private const val DOCUMENTATION_URL = "https://flounder.dev/duplicate-finder/"

fun indexAndFind(options: DuplicateFinderOptions): DuplicateFinderReport {
    val index = Index.getInstance(options)
    val indexDuration = measureTime { index.indexDirectory() }
    val (duplicates, findDuration) = measureTimedValue { findAll(options) }

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
            Option("p", "parser", true, "parser (line, file, xml, md, adoc, auto), default: auto"),
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
            "parser" to "auto",
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
        val parserOption = cmdOrDefault("parser")
        val ngramLength = cmdOrDefault("gram").toInt()

        val availableParsers = setOf("line", "file", "xml", "md", "adoc", "auto")
        require(parserOption in availableParsers) {
            "Invalid parser: $parserOption. Allowed values are: $availableParsers"
        }
        val parser = when (parserOption) {
            "line" -> ParserType.LINE
            "file" -> ParserType.FILE
            "xml" -> ParserType.XML
            "md" -> ParserType.MARKDOWN
            "adoc" -> ParserType.ASCIIDOC
            "auto" -> ParserType.AUTO
            else -> {
                System.err.println("Unsupported parser: $parserOption, defaulting to 'auto'")
                ParserType.AUTO
            }
        }

        val options = DuplicateFinderOptions(
            root,
            minSimilarity,
            minLength,
            minDuplicates,
            fileMask,
            parser,
            verbose,
            lowMemory,
            ngramLength,
            outputPath
        )

        val report = indexAndFind(options)
        if (verbose) {
            println("Indexing took: ${report.indexDuration}")
            println("Analysis took: ${report.analysisDuration}")
        }
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
