package finder.ngram

import finder.Length
import finder.Ngram
import java.util.concurrent.ConcurrentHashMap

class CachingNgramProvider private constructor(ngramLength: Length) : NgramProvider {
    companion object {
        @Volatile
        private var instance: CachingNgramProvider? = null

        fun getInstance(ngramLength: Length) = instance ?: synchronized(this) {
            instance ?: CachingNgramProvider(ngramLength).also { instance = it }
        }
    }

    private val cache = ConcurrentHashMap<String, Set<Ngram>>()
    private val computeProvider = ComputeNgramProvider.getInstance(ngramLength)

    override fun ngrams(text: String): Set<String> = cache.getOrPut(text) { computeProvider.ngrams(text) }
    override fun ngramsOrdered(text: String): List<String> = computeProvider.ngramsOrdered(text)
}
