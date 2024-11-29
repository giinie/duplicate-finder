package finder.ui

import finder.indexing.Chunk
import javax.swing.*

class ChunkList(model: DefaultListModel<Chunk>): JList<Chunk>(model) {
    init {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        visibleRowCount = -1
        font = MONOSPACE_FONT
    }
}