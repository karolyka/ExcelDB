package exceptions

/**
 * This exception will be thrown when a row doesn't exist in the Excel sheet
 *
 * @param rowIndex Index of row
 * */
class RowNotFoundException(rowIndex: Int) : GeneralExcelException("Row not found! [$rowIndex]")
