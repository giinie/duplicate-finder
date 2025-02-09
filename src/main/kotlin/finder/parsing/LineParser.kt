package finder.parsing

import finder.*

class LineParser(options: DuplicateFinderOptions): ContentParser(options) {

    override fun parse(content: String) = content.lines()
        .withIndex()
        .map { (number, line) -> Element(line, number, "line")}
        .toList()
}