package finder

import finder.indexing.Chunk
import kotlin.time.Duration

data class DuplicateFinderReport(
    val duplicates: Map<Chunk, List<Chunk>>,
    val indexDuration: Duration,
    val analysisDuration: Duration
)