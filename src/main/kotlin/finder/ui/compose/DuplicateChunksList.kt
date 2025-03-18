@file:Suppress("FunctionName")

package finder.ui.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material.MaterialTheme.colors
import finder.indexing.Chunk
import finder.similarity.similarity

@Composable
fun RowScope.DuplicateChunksList(
    duplicateChunks: List<Chunk>,
    selectedDuplicate: MutableState<Chunk?>,
    selectedReference: Chunk,
) {
    val options by LocalOptions.current
    val sorted = duplicateChunks.map { it to it.similarity(selectedReference, options)}
        .sortedByDescending { it.second }

    LazyColumn(
        Modifier.border(1.0.dp, Color.Gray)
            .fillMaxHeight()
            .weight(0.3f)
            .padding(4.dp)
    ) {
        items(sorted) { (chunk, similarity) ->
            Text(
                text = "$similarity% ${chunk.preview}",
                fontSize = LocalFontSize.current.value.size.sp,
                modifier = Modifier.fillMaxWidth()
                    .background(if (chunk == selectedDuplicate.value) { colors.secondary } else { Color.Transparent })
                    .padding(vertical = 4.dp)
                    .clickable { selectedDuplicate.value = chunk }
            )
        }
    }
}