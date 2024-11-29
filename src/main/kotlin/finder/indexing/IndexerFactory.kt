package finder.indexing

import finder.DuplicateFinderOptions
import finder.indexing.IndexerType.*

const val autoDetectFailMessage = ("""
Couldn't match indexer type by file mask, defaulting to 'file' indexer.
Use the '-i' command-line option to specify the indexer type explicitly.
""")

val markdownFileExtensions = setOf("md", "mdx")

fun indexer(options: DuplicateFinderOptions) = when (options.indexerType) {
    FILE -> FileIndexer(options)
    LINE -> LineIndexer(options)
    MARKDOWN -> MarkdownIndexer(options)
    XML -> XmlIndexer(options)
    AUTO -> {
        when {
            options.fileMaskIncludesOnly("xml") -> XmlIndexer(options)
            options.fileMaskIsSubsetOf(markdownFileExtensions) -> MarkdownIndexer(options)
            else -> {
                System.err.println(autoDetectFailMessage)
                FileIndexer(options)
            }
        }
    }
}



