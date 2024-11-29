package finder.indexing

import finder.*
import java.io.FileInputStream
import java.nio.file.Path
import javax.xml.stream.*

class XmlIndexer(
    options: DuplicateFinderOptions,
    val skipTags: List<String> = emptyList(),
): ContentIndexer(options) {

    override fun indexFile(pathFromRoot: Path): Map<Length, Map<Ngram, List<Chunk>>> {
        val path = root.resolve(pathFromRoot)
        try {

            val xmlInputFactory = XMLInputFactory.newInstance()
            val xmlStreamReader = xmlInputFactory.createXMLStreamReader(FileInputStream(path.toFile()))
            val groupedByLength = mutableMapOf<Int, MutableMap<String, MutableList<Chunk>>>()

            var currentTagName: String? = null
            val stack = ArrayDeque<String>()
            val contentBuilder = StringBuilder()

            while (xmlStreamReader.hasNext()) {
                when (xmlStreamReader.next()) {
                    XMLStreamConstants.START_ELEMENT -> {
                        currentTagName = xmlStreamReader.localName
                        stack.addLast(currentTagName)
                        contentBuilder.clear()
                    }

                    XMLStreamConstants.CHARACTERS -> {
                        contentBuilder.append(xmlStreamReader.text)
                    }

                    XMLStreamConstants.END_ELEMENT -> {
                        if (contentBuilder.length < options.minLength) {
                            continue
                        }
                        if (currentTagName == xmlStreamReader.localName && contentBuilder.isNotBlank() && currentTagName !in skipTags) {
                            val content = contentBuilder.toString().trim()
                            val length = content.length
                            val ngramIndex = groupedByLength.getOrPut(length) { mutableMapOf() }
                            ngramIndex.indexChunk(
                                content,
                                pathFromRoot.toString(),
                                xmlStreamReader.location.characterOffset - contentBuilder.length,
                                "xml_$currentTagName",
                                options
                            )
                        }
                        stack.removeLast()
                        currentTagName = null
                    }
                }
            }
            return groupedByLength

        } catch (e: Exception) {
            if (options.verbose) System.err.println("Error parsing xml file: $path ${e.javaClass.name}")
            return emptyMap()
        }
    }
}
