package finder.indexing

import finder.*
import finder.parsing.parser
import java.nio.file.Path
import kotlin.io.path.readText

class FileProcessor(val options: DuplicateFinderOptions) {
    val parser = parser(options)

    fun fileToChunks(path: Path): List<Chunk> {
        val content = path.readText()

        return try {
            val pathFromRoot = options.root.relativize(path)
            parser.parse(content).map { Chunk.of(it, pathFromRoot) }
        } catch (e: Exception) {
            if (options.verbose) System.err.println("Error parsing file: $path ${e.javaClass.name}")
            emptyList()
        }
    }
}
