package finder.parsing

import finder.DuplicateFinderOptions
import java.io.StringReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants

class Tag(val name: String) {
    val contentBuilder = StringBuilder()
}

class XmlParser(
    options: DuplicateFinderOptions,
    val skipTags: List<String> = emptyList(),
) : ContentParser(options) {
    override fun parse(content: String): List<Element> {
        val xmlInputFactory = XMLInputFactory.newInstance()
        val xmlStreamReader = xmlInputFactory.createXMLStreamReader(StringReader(content))
        val elements = mutableListOf<Element>()
        val stack = ArrayDeque<Tag>()

        while (xmlStreamReader.hasNext()) {
            when (xmlStreamReader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    val tag = Tag(xmlStreamReader.localName)
                    stack.addLast(tag)
                }

                XMLStreamConstants.CHARACTERS -> {
                    stack.last().contentBuilder.append(xmlStreamReader.text)
                }

                XMLStreamConstants.END_ELEMENT -> {
                    val tag = stack.removeLast()
                    if (tag.contentBuilder.length < options.minLength) {
                        continue
                    }
                    if (tag.name == xmlStreamReader.localName
                        && tag.contentBuilder.isNotBlank()
                        && tag.name !in skipTags
                    ) {
                        val tagContent = tag.contentBuilder.toString().trim()
                        elements.add(
                            Element(
                                content = tagContent,
                                lineNumber = xmlStreamReader.location.lineNumber,
                                type = "xml_${tag.name}",
                            )
                        )
                    }
                }
            }
        }
        
        return elements
    }
}