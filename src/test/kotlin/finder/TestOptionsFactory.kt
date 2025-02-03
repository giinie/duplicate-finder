package finder

import finder.parsing.ParserType
import java.nio.file.Path

fun mockOptionsForNgramLength(length: Int) = DuplicateFinderOptions(
    root = Path.of("./"),
    minSimilarity = 0.8,
    minLength = 5,
    minDuplicates = 1,
    fileMask = emptySet(),
    verbose = false,
    lowMemory = false,
    ngramLength = length,
    outputDirectory = Path.of("./"),
    parserType = ParserType.AUTO
)

fun mockOptions() =  DuplicateFinderOptions(
    root = Path.of("./"),
    minSimilarity = 0.8,
    minLength = 5,
    minDuplicates = 1,
    fileMask = emptySet(),
    verbose = false,
    lowMemory = false,
    ngramLength = 3,
    outputDirectory = Path.of("./"),
    parserType = ParserType.AUTO
)