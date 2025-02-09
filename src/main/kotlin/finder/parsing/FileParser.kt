package finder.parsing

import finder.*

class FileParser(options: DuplicateFinderOptions) : ContentParser(options) {

    override fun parse(content: String) = listOf(Element(content, 0, "entire_file"))
}