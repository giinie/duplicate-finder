package finder.parsing

import finder.DuplicateFinderOptions
import finder.parsing.ParserType.*

const val autoDetectFailMessage = ("""
Couldn't match parser type by file mask, defaulting to 'file' parser.
Use the '-p' command-line option to specify the parser type explicitly.
""")

val markdownFileExtensions = setOf("md", "mdx")
val asciidocFileExtensions = setOf("adoc", "asciidoc")

fun parser(options: DuplicateFinderOptions) = when (options.parserType) {
    FILE        -> FileParser(options)
    LINE        -> LineParser(options)
    MARKDOWN    -> MarkdownParser(options)
    XML         -> XmlParser(options)
    ASCIIDOC    -> AsciiDocIndexer(options)
    PROPERTIES  -> JavaPropertiesParser(options)
    AUTO -> {
        when {
            options.fileMaskIncludesOnly("xml") -> XmlParser(options)
            options.fileMaskIsSubsetOf(markdownFileExtensions) -> MarkdownParser(options)
            options.fileMaskIsSubsetOf(asciidocFileExtensions) -> AsciiDocIndexer(options)
            options.fileMaskIncludesOnly("properties") -> JavaPropertiesParser(options)
            else -> {
                System.err.println(autoDetectFailMessage)
                FileParser(options)
            }
        }
    }
}
