package finder.ui

import finder.DuplicateFinderOptions
import finder.indexing.Chunk
import finder.sort.*
import javax.swing.DefaultListModel
import kotlin.collections.filter

class ListsData(val report: Map<Chunk, List<Chunk>>, val options: DuplicateFinderOptions) {
    val referenceChunksListModel = DefaultListModel<Chunk>()
    val duplicateChunksListModel = DefaultListModel<Chunk>()
    var sorting = SortBy.MAX_DUPLICATES
    var showInClusters = true

    init { update() }

    fun showDuplicatesFor(chunk: Chunk) {
        duplicateChunksListModel.removeAllElements()
        duplicateChunksListModel.addAll(report[chunk])
    }

    fun sort(sortBy: SortBy) {
        sorting = sortBy
        update()
    }

    fun showInClusters(value: Boolean) {
        showInClusters = value
        update()
    }

    private fun addReferenceChunks() = report
        .toList()
        .filterClustered()
        .sortedWith(chunkComparator(sorting, options))
        .forEach { referenceChunksListModel.addElement(it.first) }


    private fun List<Pair<Chunk, List<Chunk>>>.filterClustered(): List<Pair<Chunk, List<Chunk>>> {
        if (!showInClusters) return this
        val seen = mutableSetOf<Chunk>()
        return this.filter { (reference, duplicates) ->
            seen.addAll(duplicates)
            reference !in seen
        }
    }

    private fun clear() {
        referenceChunksListModel.removeAllElements()
        duplicateChunksListModel.removeAllElements()
    }

    private fun update() {
        clear()
        addReferenceChunks()
    }
}
