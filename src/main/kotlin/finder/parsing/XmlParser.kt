package finder.parsing

import finder.*
import java.io.StringReader
import javax.xml.stream.*

class XmlParser(
    options: DuplicateFinderOptions,
    val skipTags: List<String> = emptyList(),
) : ContentParser(options) {

    override fun parse(content: String): List<Element> {

        val xmlInputFactory = XMLInputFactory.newInstance()
        val xmlStreamReader = xmlInputFactory.createXMLStreamReader(StringReader(content))
        val elements = mutableListOf<Element>()

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
                    if (contentBuilder.length < minLength) {
                        continue
                    }
                    if (currentTagName == xmlStreamReader.localName && contentBuilder.isNotBlank() && currentTagName !in skipTags) {
                        val tagContent = contentBuilder.toString().trim()
                        elements.add(
                            Element(
                                content = tagContent,
                                lineNumber = xmlStreamReader.location.lineNumber,
                                type = "xml_$currentTagName",
                            )
                        )
                    }
                    stack.removeLast()
                    currentTagName = null
                }
            }
        }
        return elements
    }
}
