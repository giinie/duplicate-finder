package finder.parsing

data class Element(
    val content: String,
    val lineNumber: Int,
    @Suppress("unused") val type: String,
)