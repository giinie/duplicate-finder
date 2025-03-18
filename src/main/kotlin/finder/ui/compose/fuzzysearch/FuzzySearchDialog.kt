@file:Suppress("FunctionName")

package finder.ui.compose.fuzzysearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import finder.indexing.Chunk
import finder.ui.compose.LocalOptions

@Composable
fun FuzzySearchDialog(onDismiss: () -> Unit) {
    val options by LocalOptions.current
    val queryText = remember { mutableStateOf("") }
    val minSimilarity = remember { mutableStateOf(options.minSimilarity) }
    val results = remember { mutableStateOf<List<Chunk>>(emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.8f)
                .background(Color.White),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                FuzzySearchQuery(queryText, minSimilarity, results)
                if (results.value.isNotEmpty()) FuzzySearchResults(
                    queryText = queryText.value,
                    results = results.value,
                    options = LocalOptions.current.value,
                )
            }
        }
    }
}


