package exceptions

/**
 * This exception will be thrown when a cell contains `null` but it requires `non-null` value
 *
 * @param columnName Name of the column
 */
class NullValueException(columnName: String) : GeneralExcelException("Null value does not allowed! [$columnName]")
