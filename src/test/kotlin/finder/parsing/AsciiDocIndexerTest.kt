package finder.parsing

import finder.mockOptions
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AsciiDocIndexerTest {
    @Test
    fun `test parsing sample asciidoc file with indexer`() {
        val options = mockOptions()
        
        // Parse with regular AsciiDocParser
        val regularParser = AsciiDocParser(options)
        val regularResult = regularParser.parse(Path.of("src/test/resources/test-document.adoc").readText())
        
        // Parse with AsciiDocIndexer
        val indexer = AsciiDocIndexer(options)
        val indexerResult = indexer.parse(Path.of("src/test/resources/test-document.adoc").readText())
        
        // Verify that both parsers return the same elements (though possibly in different order)
        assertEquals(regularResult.size, indexerResult.size, 
            "AsciiDocIndexer should parse the same number of elements as AsciiDocParser")
        
        // Verify that all elements from the regular parser are in the indexer result
        regularResult.forEach { element ->
            assertTrue(indexerResult.any { 
                it.content == element.content && 
                it.lineNumber == element.lineNumber && 
                it.type == element.type 
            }, "Element should be present in indexer result: $element")
        }
        
        // Verify that the indexer result is sorted
        for (i in 0 until indexerResult.size - 1) {
            val current = indexerResult[i]
            val next = indexerResult[i + 1]
            
            // Check if sorted by type, then by line number, then by content
            val comparison = compareBy<Element>({ it.type }, { it.lineNumber }, { it.content })
                .compare(current, next)
            
            assertTrue(comparison <= 0, 
                "Elements should be sorted: $current should come before $next")
        }
        
        println("[DEBUG_LOG] Parsed chunks from indexer:")
        indexerResult.forEach { chunk ->
            println("[DEBUG_LOG]   ${chunk.type}: '${chunk.content}'")
        }
    }
    
    @Test
    fun `test parsing recommended practices document with indexer`() {
        val options = mockOptions()
        val indexer = AsciiDocIndexer(options)
        val result = indexer.parse(Path.of("src/test/resources/asciidoc-recommended-practices.adoc").readText())
        
        assertTrue(result.isNotEmpty(), "Should have parsed some content from the recommended practices document")
        
        println("[DEBUG_LOG] Parsed chunks from recommended practices document:")
        println("[DEBUG_LOG] Total chunks: ${result.size}")
        
        // Group by type and count
        val typeCount = result.groupBy { it.type }.mapValues { it.value.size }
        println("[DEBUG_LOG] Chunks by type: $typeCount")
        
        // Print first few elements of each type
        result.groupBy { it.type }.forEach { (type, elements) ->
            println("[DEBUG_LOG] Type: $type (${elements.size} elements)")
            elements.take(3).forEach { element ->
                println("[DEBUG_LOG]   Line ${element.lineNumber}: '${element.content.take(50)}${if (element.content.length > 50) "..." else ""}'")
            }
        }
    }
}