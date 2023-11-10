package exceptions

/**
 * This exception will be thrown when a key does not exist
 *
 * @param value Name of the column
 */
class KeyNotFoundException(value: String?) : GeneralExcelException("Key not found! [$value]")
