package finder.indexing

import finder.*
import java.nio.file.Path

abstract class ContentIndexer(val options: DuplicateFinderOptions) {

    val root: Path
        get() = options.root

    abstract fun indexFile(pathFromRoot: Path): Map<Length, Map<Ngram, List<Chunk>>>
}