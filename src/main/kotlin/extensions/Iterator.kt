package extensions

/**
 * This method iterates all element and invoke the given action with current element
 *
 * @param T
 * @param action
 * */
fun <T> Iterator<T>.iterate(action: (T) -> Unit = {}) {
    while (hasNext())
        action(next())
}
