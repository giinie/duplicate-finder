package finder.parsing

import finder.DuplicateFinderOptions
import java.util.TreeSet

/**
 * AsciiDoc indexer that stores parsed elements in a sorted collection
 * and provides a way to review the chunk list before returning.
 */
class AsciiDocIndexer(options: DuplicateFinderOptions) : ContentParser(options) {

    private val asciiDocParser = AsciiDocParser(options)

    /**
     * Parse the content and store the elements in a sorted collection.
     * This allows for setting breakpoints and reviewing the chunk list before returning.
     */
    override fun parse(content: String): List<Element> {
        // Parse the content using the AsciiDocParser
        val elements = asciiDocParser.parse(content)

        // Create a sorted collection of elements
        val sortedElements = TreeSet<Element>(compareBy({ it.type }, { it.lineNumber }, { it.content }))

        // Add all elements to the sorted collection
        sortedElements.addAll(elements)

        // Convert the sorted collection back to a list
        return sortedElements.toList()
    }
}
