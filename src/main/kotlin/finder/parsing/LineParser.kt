package finder.parsing

import finder.*

class LineParser(options: DuplicateFinderOptions): ContentParser(options) {

    override fun parse(content: String) = content.lines()
        .withIndex()
        .filter { it.value.length >= minLength}
        .map { line -> Element(line.value, line.index, "line")}
        .toList()
}