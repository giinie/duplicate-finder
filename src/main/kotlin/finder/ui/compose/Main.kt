@file:Suppress("FunctionName")

package finder.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import finder.indexing.Chunk
import finder.ui.sort.*
import finder.ui.utils.*
import kotlinx.coroutines.*

@Composable
fun Main(
    report: Map<Chunk, List<Chunk>>,
) {
    val sorting by LocalSorting.current
    val showInClusters by LocalShowInClusters.current
    val options by LocalOptions.current

    val (entries, setEntries) = remember(showInClusters, sorting) { mutableStateOf<List<Pair<Chunk, List<Chunk>>>?>(null) }

    LaunchedEffect(report) {
        launch(Dispatchers.Default) {
            setEntries(
                report.filterClustered(showInClusters)
                    .toList()
                    .sortedWith(chunkComparator(sorting, options))
            )
        }
    }

    if (entries == null) {
        Text("Loading...")
    } else {
        val selectedReference = remember(entries) { mutableStateOf<Chunk>(entries.firstReference()) }
        val duplicateChunks = remember(selectedReference.value) { entries.duplicates(selectedReference.value) }
        val selectedDuplicate = remember { mutableStateOf<Chunk?>(duplicateChunks.first()) }

        Row {
            ReferenceChunksList(entries, selectedReference, selectedDuplicate)
            DuplicateChunksList(duplicateChunks, selectedDuplicate, selectedReference.value)
            ChunksPreview(selectedReference, selectedDuplicate, duplicateChunks)
        }
    }
}

private fun List<Pair<Chunk, List<Chunk>>>.firstReference() = first().first

private fun List<Pair<Chunk, List<Chunk>>>.duplicates(reference: Chunk) = single { it.first == reference }.second