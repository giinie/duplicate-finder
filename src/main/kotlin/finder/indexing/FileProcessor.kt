package finder.indexing

import finder.*
import finder.ngram.ngramProvider
import finder.parsing.parser
import java.nio.file.Path
import kotlin.io.path.readText

class FileProcessor(val options: DuplicateFinderOptions) {
    val ngramProvider = ngramProvider(options)
    val parser = parser(options)

    fun indexFile(pathFromRoot: Path): Map<Length, Map<Ngram, List<Chunk>>> {
        val path = options.root.resolve(pathFromRoot)
        val content = path.readText()
        if (content.length < options.minLength) return emptyMap()

        val blocks = try {
            parser.parse(content)
        } catch (e: Exception) {
            if (options.verbose) System.err.println("Error parsing file: $path ${e.javaClass.name}")
            return emptyMap()
        }

        return buildMap<Int, MutableMap<String, MutableList<Chunk>>> {
            blocks.forEach {
                val length = it.content.length
                val chunk = Chunk.of(it, pathFromRoot)
                val forLength = getOrPut(length) { mutableMapOf() }
                val ngrams = ngramProvider.ngrams(it.content)
                ngrams.forEach { ngram ->
                    val forNgram = forLength.getOrPut(ngram) { mutableListOf() }
                    forNgram.add(chunk)
                }
            }
        }
    }
}
