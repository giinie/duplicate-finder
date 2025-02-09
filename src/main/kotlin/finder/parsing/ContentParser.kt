package finder.parsing

import finder.*
import java.nio.file.Path

abstract class ContentParser(val options: DuplicateFinderOptions) {

    val root: Path
        get() = options.root

    abstract fun parse(content: String): List<Element>
}