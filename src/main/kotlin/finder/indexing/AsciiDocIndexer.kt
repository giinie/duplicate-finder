package finder.indexing

import finder.*
import finder.parsing.AsciiDocParser
import java.nio.file.Path

class AsciiDocIndexer(options: DuplicateFinderOptions) : ContentIndexer(options) {
    private val parser = AsciiDocParser()

    override fun indexFile(pathFromRoot: Path): Map<Length, Map<Ngram, List<Chunk>>> {
        val path = root.resolve(pathFromRoot)
        val chunks = parser.parse(path)

        return chunks
            .filter { it.content.length >= options.minLength }
            .groupBy { it.content.length }
            .mapValues { (_, chunksOfLength) ->
                buildMap<Ngram, MutableList<Chunk>> {
                    chunksOfLength.forEachIndexed { index, asciiDocChunk ->
                        indexChunk(
                            asciiDocChunk.content,
                            "${pathFromRoot}#${asciiDocChunk.lineNumber}",
                            asciiDocChunk.lineNumber,
                            asciiDocChunk.type,
                            options
                        )
                    }
                }
            }
    }
}
