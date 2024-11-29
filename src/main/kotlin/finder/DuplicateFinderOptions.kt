package finder

import finder.indexing.*
import java.nio.file.Path
import kotlin.io.path.extension

data class DuplicateFinderOptions(
    val root: Path,
    val minSimilarity: Double,
    val minLength: Int,
    val minDuplicates: Int,
    val fileMask: Set<String>,
    val indexerType: IndexerType,
    val verbose: Boolean,
    val lowMemory: Boolean,
    val ngramLength: Int,
    val outputDirectory: Path
) {
    fun fileMaskIncludes(path: Path) = fileMask.isEmpty() || path.extension in fileMask

    fun fileMaskIncludesOnly(extension: String) = fileMask.size == 1 && fileMask.single() == extension

    fun fileMaskIsSubsetOf(extensions: Set<String>) = (fileMask - extensions).isEmpty()
}