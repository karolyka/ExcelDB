package extensions

internal fun String.normalizeFieldName(): String =
    this
        .lowercase()
        .map {
            when (it) {
                in '0'..'9' -> it
                in 'a'..'z' -> it
                'é' -> 'e'
                'á' -> 'a'
                'í' -> 'i'
                in "öőó" -> 'o'
                in "üűú" -> 'u'
                in " _()[]" -> '_'
                else -> ""
            }
        }.joinToString("")
