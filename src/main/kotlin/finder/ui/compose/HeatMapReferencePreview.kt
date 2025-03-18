@file:Suppress("FunctionName")

package finder.ui.compose

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import finder.DuplicateFinderOptions
import finder.indexing.Chunk
import finder.ui.utils.charsToScores

@Composable
fun HeatMapReferencePreview(
    options: DuplicateFinderOptions,
    baseChunk: Chunk,
    duplicateChunks: List<Chunk>,
) {
    val scores = charsToScores(options, baseChunk, duplicateChunks)
    val builder = AnnotatedString.Builder()

    scores.forEach { (char, score) ->
        builder.apply {
            withStyle(
                style = SpanStyle(
                    color = Color.hsv(
                        hue = 120f * score,
                        saturation = 1.0f,
                        value = 0.6f
                    ),

                )
            ) { append(char) }
        }
    }
    SelectionContainer {
        Text(
            text = builder.toAnnotatedString(),
            fontSize = LocalFontSize.current.value.size.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}