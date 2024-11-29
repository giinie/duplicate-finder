package finder.sort

enum class SortBy(val description: String) {
    MAX_DUPLICATES("Number of duplicates"),
    MAX_LENGTH("Length"),
    MAX_AVG_SIMILARITY("Average similarity");

    override fun toString() = description
}