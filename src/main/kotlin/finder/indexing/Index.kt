package finder.indexing

import finder.*
import finder.ngram.ngramProvider
import java.nio.file.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.isRegularFile

class Index private constructor(val options: DuplicateFinderOptions) {

    val ngramProvider = ngramProvider(options)

    companion object {
        @Volatile
        private var instance: Index? = null

        fun getInstance(options: DuplicateFinderOptions) = instance ?: synchronized(this) {
            instance ?: Index(options).also { instance = it }
        }
    }

    private val directoryIndex = ConcurrentHashMap<Length, MutableMap<Ngram, MutableList<Chunk>>>()

    fun chunksFlat(): List<Chunk> = directoryIndex.values.flatMap { it.values }.flatten().distinct()

    fun getForLength(length: Int) = directoryIndex.computeIfAbsent(length) { mutableMapOf<Ngram, MutableList<Chunk>>() }

    fun indexDirectory() {
        val (root, _, _, _, _, _, verbose) = options
        val fileCount = AtomicInteger(0)
        val filesToIndex = filesToIndex(root, options)
        if (verbose) println("Indexing ${filesToIndex.size} files")

        filesToIndex.parallelStream()
            .peek { if (verbose) println("processing file ${fileCount.incrementAndGet()}: $it") }
            .forEach { indexFile(it) }
    }

    private fun indexFile(path: Path) {
        val fileProcessor = FileProcessor(options)
        val chunks = fileProcessor.fileToChunks(path)
        chunks.forEach { indexChunk(it) }
    }

    private fun indexChunk(chunk: Chunk) {
        val ngrams = ngramProvider.ngrams(chunk.content)
        val forLength = getForLength(chunk.content.length)
        synchronized (forLength) {
            ngrams.forEach { ngram ->
                val forNgram = forLength.computeIfAbsent(ngram) { mutableListOf() }
                forNgram.add(chunk)
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
}