package finder.ngram

interface NgramProvider {
    fun ngrams(text: String): Set<String>
    fun ngramsOrdered(text: String): List<String>
}