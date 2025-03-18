package finder.ui.sort

import finder.DuplicateFinderOptions
import finder.indexing.Chunk
import finder.ngram.ngramProvider
import finder.similarity.similarityRatio
import finder.ui.sort.SortBy.*

fun chunkComparator(
    sortBy: SortBy,
    options: DuplicateFinderOptions
): Comparator<Pair<Chunk, List<Chunk>>> = when (sortBy) {
    MAX_DUPLICATES -> Comparator.comparingInt<Pair<Chunk, List<Chunk>>> { it.second.size }.reversed()
    MAX_LENGTH -> Comparator.comparingInt<Pair<Chunk, List<Chunk>>> { it.first.content.length }.reversed()
    MAX_AVG_SIMILARITY -> maxAvgSimilarityComparator(options)
}

fun maxAvgSimilarityComparator(options: DuplicateFinderOptions): Comparator<Pair<Chunk, List<Chunk>>> {
    val ngramProvider = ngramProvider(options)

    val avgSimilarityDesc = Comparator.comparingDouble<Pair<Chunk, List<Chunk>>> { (reference, duplicates) ->
        duplicates.map { duplicate ->
            val duplicateNgrams = ngramProvider.ngrams(duplicate.content)
            val referenceNgrams = ngramProvider.ngrams(reference.content)
            similarityRatio(duplicateNgrams, referenceNgrams)
        }.average()
    }.reversed()

    val numDuplicatesDesc = chunkComparator(MAX_DUPLICATES, options)

    return avgSimilarityDesc.thenComparing(numDuplicatesDesc)
}
