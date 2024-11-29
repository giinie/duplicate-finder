package finder

import finder.indexing.*
import finder.ngram.ngramProvider
import finder.similarity.similarityRatio
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.collections.*
import kotlin.math.max

fun find(
    directoryIndex: Map<Length, Map<Ngram, List<Chunk>>>,
    options: DuplicateFinderOptions
): Map<Chunk, List<Chunk>> {
    val (_, _, _, minDuplicates, _, _, verbose) = options
    val chunksFlat = directoryIndex.values.flatMap { it.values }.flatten().distinct()
    val processedChunksCount = AtomicInteger(0)
    return chunksFlat.parallelStream()
        .collect(
            Collectors.toMap(
                Function.identity(),
                {
                    if (verbose && processedChunksCount.incrementAndGet() % 100 == 0) {
                        println("Searching duplicates for chunk ${processedChunksCount.get()}/${chunksFlat.size}")
                    }
                    findInLengthIndex(it, directoryIndex, options)
                },
                { _, _ -> throw RuntimeException("Chunk already analyzed") },
            )
        )
        .filter { it.value.size >= minDuplicates.coerceAtLeast(1) }
}

private fun findInLengthIndex(
    referenceChunk: Chunk,
    directoryIndex: Map<Length, Map<Ngram, List<Chunk>>>,
    options: DuplicateFinderOptions,
): List<Chunk> {
    val length = referenceChunk.content.length
    val margin = (length - (length * options.minSimilarity)).toInt()
    val minLength = length - margin
    val maxLength = length + margin
    return buildList {
        directoryIndex.filter { (length, _) -> length in minLength..maxLength }
            .forEach { (_, indexForLength) ->
                val resultsForLength = findInNgramIndex(referenceChunk, indexForLength, options)
                addAll(resultsForLength)
            }
    }
}

private fun findInNgramIndex(
    referenceChunk: Chunk,
    index: Map<Ngram, List<Chunk>>,
    options: DuplicateFinderOptions
): List<Chunk> {
    val ngramProvider = ngramProvider(options)
    val thisNgrams = ngramProvider.ngrams(referenceChunk.content)
    val scores = Object2IntOpenHashMap<Chunk>()
    val minScoreFilter = (thisNgrams.size * options.minSimilarity).toInt()
    var currentMaxScore = 0

    for ((evaluatedNgrams, ngram) in thisNgrams.withIndex()) {
        val remainingNgrams = thisNgrams.size - evaluatedNgrams
        val chunksWithNgram = (index[ngram] ?: emptyList())
        for (other in chunksWithNgram) {
            if (other === referenceChunk) continue
            val score = scores.getInt(other) + 1
            scores.put(other, score)
            currentMaxScore = max(score, currentMaxScore)
        }
        if (currentMaxScore + remainingNgrams < minScoreFilter) return emptyList()
    }

    val duplicates = buildList {
        scores.object2IntEntrySet().fastForEach { (candidate, score) ->
            if (score < minScoreFilter) return@fastForEach
            val maxNgrams = max(ngramProvider.ngrams(candidate.content).size, thisNgrams.size)
            if (similarityRatio(score, maxNgrams) >= options.minSimilarity) {
                add(candidate)
            }
        }
    }

    return duplicates
}