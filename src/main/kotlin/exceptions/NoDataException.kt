package exceptions

/** This exception will be thrown when trying to write an empty workbook to disk */
class NoDataException : GeneralExcelException("There is no sheet")
