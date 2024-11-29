package finder.ngram

import finder.Length

class ComputeNgramProvider private constructor(private val ngramLength: Int) : NgramProvider {

    companion object {
        @Volatile
        private var instance: ComputeNgramProvider? = null

        fun getInstance(ngramLength: Length) = instance ?: synchronized(this) {
            instance ?: ComputeNgramProvider(ngramLength).also { instance = it }
        }
    }

    override fun ngrams(text: String) = text.toNgramsCollection(ngramLength, mutableSetOf())
    override fun ngramsOrdered(text: String) = text.toNgramsCollection(ngramLength, mutableListOf())
}

private fun <T : MutableCollection<String>> String.toNgramsCollection(ngramLength: Int, collection: T): T {
    if (this.length >= ngramLength) {
        for (i in 0..this.length - ngramLength) {
            val gram = this.substring(i, i + ngramLength)
            collection.add(gram)
        }
    }
    return collection
}


