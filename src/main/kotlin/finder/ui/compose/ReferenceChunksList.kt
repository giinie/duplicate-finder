@file:Suppress("FunctionName")

package finder.ui.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import finder.indexing.Chunk

@Composable
fun RowScope.ReferenceChunksList(
    entries: List<Pair<Chunk, List<Chunk>>>,
    selectedReference: MutableState<Chunk>,
    selectedDuplicate: MutableState<Chunk?>,
) = LazyColumn(
    Modifier.border(1.0.dp, Color.Gray)
        .fillMaxHeight()
        .weight(0.3f)
        .padding(4.dp)
) {
    items(entries) { entry ->
        Text(
            text = "(${entry.numDuplicates}) ${entry.reference.preview}",
            fontSize = LocalFontSize.current.value.size.sp,
            modifier = Modifier.fillMaxWidth()
                .background(if (entry.reference == selectedReference.value) { colors.secondary } else { Color.Transparent })
                .padding(vertical = 4.dp)
                .clickable {
                    selectedReference.value = entry.reference
                    selectedDuplicate.value = null
                }
        )
    }
}

private val Pair<Chunk, List<Chunk>>.reference
    get() = first

private val Pair<Chunk, List<Chunk>>.numDuplicates
    get() = second.size