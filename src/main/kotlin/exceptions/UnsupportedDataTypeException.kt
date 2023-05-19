package exceptions

/** This exception will be thrown when a property has unsupported type */
class UnsupportedDataTypeException(propertyName: String?) :
    GeneralExcelException("Unsupported data type for property [$propertyName]")
