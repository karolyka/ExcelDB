package exceptions

/**
 * This exception will be thrown when a column doesn't exist for a property
 *
 * @param columnName Name of the column
 */
class ColumnNotFoundException(columnName: String) : GeneralExcelException("Column not found! [$columnName]")
