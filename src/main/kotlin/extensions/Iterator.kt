package extensions

/** Maps data received given from [iterator] to [MutableList] */
fun <T> Iterator<T>.toList(): MutableList<T> = mutableListOf<T>().also { result -> forEach { result.add(it) } }
