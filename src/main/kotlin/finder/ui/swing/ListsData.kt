package finder.ui.swing

import finder.DuplicateFinderOptions
import finder.indexing.Chunk
import finder.ui.sort.SortBy
import finder.ui.sort.chunkComparator
import finder.ui.utils.filterClustered
import javax.swing.DefaultListModel

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
        .filterClustered(filter = showInClusters)
        .toList()
        .sortedWith(chunkComparator(sorting, options))
        .forEach { referenceChunksListModel.addElement(it.first) }

    private fun clear() {
        referenceChunksListModel.removeAllElements()
        duplicateChunksListModel.removeAllElements()
    }

    private fun update() {
        clear()
        addReferenceChunks()
    }
}