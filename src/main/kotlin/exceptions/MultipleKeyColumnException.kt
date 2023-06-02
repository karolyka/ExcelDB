package exceptions

/**
 * This exception will be thrown when there are more key columns than 1
 *
 * @param columnNames Name of the columns
 */
class MultipleKeyColumnException(columnNames: String) : GeneralExcelException("Multiple key columns! [$columnNames]")
