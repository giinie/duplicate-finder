package finder.ngram

import finder.DuplicateFinderOptions

fun ngramProvider(options: DuplicateFinderOptions): NgramProvider {
    return if (options.lowMemory) {
        ComputeNgramProvider.getInstance(options.ngramLength)
    } else {
        CachingNgramProvider.getInstance(options.ngramLength)
    }
}