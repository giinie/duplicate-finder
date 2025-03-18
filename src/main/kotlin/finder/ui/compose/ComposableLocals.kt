package finder.ui.compose

import androidx.compose.runtime.*
import finder.DuplicateFinderOptions
import finder.ui.sort.SortBy

val LocalFontSize = compositionLocalOf { mutableStateOf(FontSize.MEDIUM) }
val LocalSorting = compositionLocalOf { mutableStateOf(SortBy.MAX_DUPLICATES) }
val LocalShowInClusters = compositionLocalOf { mutableStateOf(true) }
lateinit var LocalOptions: ProvidableCompositionLocal<MutableState<DuplicateFinderOptions>>