package finder.parsing

import finder.DuplicateFinderOptions
import finder.parsing.ParserType.*

const val autoDetectFailMessage = ("""
Couldn't match parser type by file mask, defaulting to 'file' parser.
Use the '-i' command-line option to specify the parser type explicitly.
""")

val markdownFileExtensions = setOf("md", "mdx")
val asciidocFileExtensions = setOf("adoc", "asciidoc")

fun parser(options: DuplicateFinderOptions) = when (options.parserType) {
    FILE        -> FileParser(options)
    LINE        -> LineParser(options)
    MARKDOWN    -> MarkdownParser(options)
    XML         -> XmlParser(options)
    ASCIIDOC    -> AsciiDocParser(options)
    AUTO -> {
        when {
            options.fileMaskIncludesOnly("xml") -> XmlParser(options)
            options.fileMaskIsSubsetOf(markdownFileExtensions) -> MarkdownParser(options)
            options.fileMaskIsSubsetOf(asciidocFileExtensions) -> AsciiDocParser(options)
            else -> {
                System.err.println(autoDetectFailMessage)
                FileParser(options)
            }
        }
    }
}
