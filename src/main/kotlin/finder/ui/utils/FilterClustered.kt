package finder.ui.utils

import finder.indexing.Chunk

fun Map<Chunk, List<Chunk>>.filterClustered(filter: Boolean): Map<Chunk, List<Chunk>> {
    if (!filter) return this

    val copy = this.toMap()
    val seen = mutableSetOf<Chunk>()

    return copy.filter { (reference, duplicates) ->
        seen.addAll(duplicates)
        reference !in seen
    }
}