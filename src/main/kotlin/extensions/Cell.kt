package extensions

import exceptions.UnsupportedCellTypeException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFCell

private const val CELL_CONTAINS_ERROR = "#ERROR"

/** Get the type of cell */
fun Cell.cellType(): CellType = if (cellType == CellType.FORMULA) cachedFormulaResultType else cellType

/** Get the value of [Cell] as [String]? */
fun Cell.asString(): String? {
    return when (cellType()) {
        CellType._NONE -> throw UnsupportedCellTypeException()
        CellType.BLANK -> null
        CellType.BOOLEAN -> booleanCellValue.toString()
        CellType.ERROR -> (this as? XSSFCell)?.errorCellString ?: CELL_CONTAINS_ERROR
        CellType.FORMULA -> throw UnsupportedCellTypeException()
        CellType.NUMERIC -> numericCellValue.toString()
        CellType.STRING -> stringValue
    }
}

/** A property for cell, it contains the [Cell.getStringCellValue] as [String]? */
val Cell.stringValue: String?
    get() {
        return (
            try {
                this.stringCellValue
            } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception) { // TODO
                (this as XSSFCell).rawValue
            }
            )?.trim()
    }

/** Get a value of the [Cell] as [Int]? */
internal fun Cell.intCellValue() = this.asString()?.toDouble()?.toInt()
