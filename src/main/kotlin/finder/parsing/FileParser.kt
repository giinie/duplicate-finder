package finder.parsing

import finder.*

class FileParser(options: DuplicateFinderOptions) : ContentParser(options) {

    override fun parse(content: String): List<Element> {
        return if (content.length >= minLength) {
            listOf(Element(content, 0, "entire_file"))
        } else emptyList()
    }
}