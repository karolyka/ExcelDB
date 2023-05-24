package exceptions

/** This exception will be thrown when there is no key field but try to find with that */
class NoKeyFieldException : GeneralExcelException("Unsupported cell type")
