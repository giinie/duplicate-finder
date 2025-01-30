package finder.parsing

import java.nio.file.Path
import kotlin.io.path.readLines

data class AsciiDocChunk(val type: String, val content: String, val lineNumber: Int)

class AsciiDocParser {
    fun parse(path: Path): List<AsciiDocChunk> {
        val lines = path.readLines()
        val chunks = mutableListOf<AsciiDocChunk>()
        var currentBlock = mutableListOf<Pair<String, Int>>()  // Pair of line content and line number
        var inCodeBlock = false
        var inTable = false
        var tableHeader = true
        var skipUntilLine = -1  // Track which lines to skip for multi-line table rows

        fun processCurrentBlock() {
            if (currentBlock.isEmpty()) return

            val content = currentBlock.map { it.first }.joinToString("\n").trim()
            val startLine = currentBlock.first().second
            if (content.isNotEmpty()) {
                when {
                    content.startsWith("= ") -> {
                        // Extract only the first line as the document title
                        val title = content.removePrefix("= ").lines().first().trim()
                        chunks.add(AsciiDocChunk("adoc_section_0", title, startLine))

                        // Process remaining lines as a separate paragraph if they exist
                        val remainingLines = content.lines().drop(1).joinToString("\n").trim()
                        if (remainingLines.isNotEmpty()) {
                            chunks.add(AsciiDocChunk("adoc_metadata", remainingLines, startLine + 1))
                        }
                    }
                    content.startsWith("== ") -> chunks.add(AsciiDocChunk("adoc_section_1", content.removePrefix("== ").trim(), startLine))
                    content.startsWith("=== ") -> chunks.add(AsciiDocChunk("adoc_section_2", content.removePrefix("=== ").trim(), startLine))
                    content.startsWith("==== ") -> chunks.add(AsciiDocChunk("adoc_section_3", content.removePrefix("==== ").trim(), startLine))
                    content.startsWith("* ") || content.startsWith("** ") -> {
                        content.lines().forEachIndexed { index, line ->
                            val itemContent = line.trim().removePrefix("*").removePrefix("*").trim()
                            if (itemContent.isNotEmpty()) {
                                chunks.add(AsciiDocChunk("adoc_list_item", itemContent, startLine + index))
                            }
                        }
                    }
                    else -> {
                        // Split content into lines and check for embedded lists
                        val lines = content.lines()
                        var currentParagraph = mutableListOf<String>()
                        var currentLineNumber = startLine

                        lines.forEach { line ->
                            if (line.trim().startsWith("* ")) {
                                // If we have accumulated paragraph content, add it first
                                if (currentParagraph.isNotEmpty()) {
                                    chunks.add(AsciiDocChunk("adoc_paragraph", currentParagraph.joinToString("\n"), currentLineNumber))
                                    currentParagraph.clear()
                                }
                                // Add the list item
                                val itemContent = line.trim().removePrefix("*").trim()
                                if (itemContent.isNotEmpty()) {
                                    chunks.add(AsciiDocChunk("adoc_list_item", itemContent, currentLineNumber))
                                }
                            } else {
                                currentParagraph.add(line)
                            }
                            currentLineNumber++
                        }

                        // Add any remaining paragraph content
                        if (currentParagraph.isNotEmpty()) {
                            chunks.add(AsciiDocChunk("adoc_paragraph", currentParagraph.joinToString("\n"), startLine))
                        }
                    }
                }
            }
            currentBlock.clear()
        }

        lines.forEachIndexed { lineNumber, line ->
            when {
                line.startsWith(".") -> {
                    processCurrentBlock()
                    chunks.add(AsciiDocChunk("adoc_table_title", line.removePrefix(".").trim(), lineNumber + 1))
                }
                line == "|===" -> {
                    processCurrentBlock()
                    inTable = !inTable
                    tableHeader = true
                }
                line == "----" -> {
                    if (inCodeBlock) {
                        val codeContent = currentBlock.map { it.first }.joinToString("\n")
                        if (codeContent.isNotEmpty()) {
                            chunks.add(AsciiDocChunk("adoc_listing", codeContent, currentBlock.first().second))
                        }
                        currentBlock.clear()
                    }
                    inCodeBlock = !inCodeBlock
                }
                inTable && line.trim().isNotEmpty() -> {
                    if (tableHeader) {
                        // Clean up header content by removing pipe characters and trimming
                        val headerContent = line.trim()
                            .split("|")
                            .filter { it.isNotEmpty() }
                            .joinToString(" | ") { it.trim() }
                        chunks.add(AsciiDocChunk("adoc_table_header", headerContent, lineNumber + 1))
                        tableHeader = false
                    } else if (line.startsWith("|")) {
                        // Skip table boundary markers and already processed lines
                        if (!line.contains("===") && lineNumber >= skipUntilLine) {
                            // Collect all cells until empty line or non-cell line
                            val cells = mutableListOf<String>()
                            var currentLineIndex = lineNumber
                            var currentLine = line

                            // Process current line and look ahead for additional cells
                            while (currentLine.startsWith("|") && !currentLine.contains("===")) {
                                cells.addAll(currentLine.split("|")
                                    .filter { it.isNotEmpty() }
                                    .map { it.trim() })

                                val nextLine = lines.getOrNull(currentLineIndex + 1)
                                if (nextLine == null || nextLine.trim().isEmpty() || !nextLine.startsWith("|")) {
                                    break
                                }
                                currentLineIndex++
                                currentLine = nextLine
                            }

                            if (cells.isNotEmpty()) {
                                chunks.add(AsciiDocChunk("adoc_table_row", cells.joinToString(" | "), lineNumber + 1))
                                // Set the skip line to after the last processed line
                                skipUntilLine = currentLineIndex + 1
                            }
                        }
                    }
                }
                inCodeBlock -> currentBlock.add(line to (lineNumber + 1))
                line.trim().isEmpty() -> processCurrentBlock()
                line.startsWith("[source") -> {
                    processCurrentBlock()
                }
                else -> currentBlock.add(line to (lineNumber + 1))
            }
        }

        processCurrentBlock()
        return chunks
    }
}
