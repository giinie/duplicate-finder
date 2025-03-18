package finder.ui.swing

import finder.DuplicateFinderOptions
import finder.indexing.Chunk
import finder.ui.utils.charsToScores
import java.awt.Color
import javax.swing.JTextPane
import javax.swing.text.*

fun heatMapDocument(
    baseChunk: Chunk,
    duplicates: List<Chunk>,
    options: DuplicateFinderOptions
): Document {
    val charsToScores = charsToScores(options, baseChunk, duplicates)
    return document(charsToScores)
}

private fun document(charsToScores: List<Pair<Char, Float>>): Document {
    return JTextPane().document.apply {
        charsToScores.forEach { (char, score) ->
            val attributes = colorAttributeFor(score)
            insertString(length, char.toString(), attributes)
        }
    }
}

private fun colorAttributeFor(hue: Float): SimpleAttributeSet {
    val shade = Color.getHSBColor(hue * 0.33f, 1.0f, 0.75f)
    val attributes = SimpleAttributeSet()
    StyleConstants.setForeground(attributes, shade)
    return attributes
}