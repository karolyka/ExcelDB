package exceptions

/**
 * This exception will be thrown when a column is not unique in the Excel sheet
 *
 * @param columnName Name of the column
 */
class NonUniqueColumnException(columnName: String) : GeneralExcelException("Non unique columns! [$columnName]")
