package exceptions

/**
 * This exception will be thrown when a sheet doesn't exist
 *
 * @param sheetName Name of the sheet
 */
class SheetNotFoundException(sheetName: String) : GeneralExcelException("Sheet not found! [$sheetName]")
