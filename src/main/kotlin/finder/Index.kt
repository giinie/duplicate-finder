package finder

import finder.indexing.Chunk
import finder.indexing.indexer
import java.nio.file.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.isRegularFile

fun indexDirectory(
    options: DuplicateFinderOptions
): Map<Length, Map<Ngram, List<Chunk>>> {
    val (root, _, _, _, _, _, verbose) = options
    val directoryIndex = ConcurrentHashMap<Length, Map<Ngram, List<Chunk>>>()
    val fileCount = AtomicInteger(0)
    val filesToIndex = filesToIndex(root, options)
    if (verbose) println("Indexing ${filesToIndex.size} files")

    val indexer = indexer(options)
    filesToIndex.parallelStream()
        .peek { if (verbose) println("processing file ${fileCount.incrementAndGet()}: $it") }
        .forEach {
            val pathFromRoot = root.relativize(it)
            val fileIndex = indexer.indexFile(pathFromRoot)
            mergeFileIndexIntoDirectoryIndex(fileIndex, directoryIndex)
        }
    return directoryIndex
}

private fun mergeFileIndexIntoDirectoryIndex(
    fileIndex: Map<Length, Map<Ngram, List<Chunk>>>,
    directoryIndex: MutableMap<Length, Map<Ngram, List<Chunk>>>
) = fileIndex.forEach { (length, ngramIndex) ->
    directoryIndex.merge(length, ngramIndex) { existingIndex, newIndex ->
        existingIndex.toMutableMap().apply {
            newIndex.forEach { (k, v) ->
                merge(k, v) { listA, listB -> listA + listB }
            }
        }
    }
}

private fun filesToIndex(
    root: Path,
    options: DuplicateFinderOptions
) = Files.walk(root)
    .parallel()
    .filter { path -> path.isRegularFile() && options.fileMaskIncludes(path) }
    .toList()