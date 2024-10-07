package extensions

import CellStyles
import CellStyles.Style.DATE
import CellStyles.Style.DATETIME
import CellStyles.Style.TIME
import exceptions.UnsupportedCellTypeException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.RichTextString
import org.apache.poi.xssf.usermodel.XSSFCell
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import kotlin.reflect.KClass

private const val CELL_CONTAINS_ERROR = "#ERROR"
private val ZERO_DAY = LocalDate.ofEpochDay(0)

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
    get() =
        try {
            this.stringCellValue
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception,
        ) {
            // TODO
            (this as XSSFCell).rawValue
        }?.trim()

/** Get a value of the [Cell] as [Int]? */
internal fun Cell.intCellValue() = this.asString()?.toDouble()?.toInt()

/** Set a value of the [Cell] */
@Suppress("CyclomaticComplexMethod")
fun Cell.setCellValue(
    value: Any?,
    cellStyles: CellStyles,
) {
    when (value) {
        is Boolean -> setCellValue(value)
        is Calendar -> setCellValue(value.time).also { cellStyle = cellStyles.getStyle(DATETIME) }
        is Date -> setCellValue(value).also { cellStyle = cellStyles.getStyle(DATE) }
        is Double -> setCellValue(value)
        is Int -> setCellValue(value.toDouble())
        is LocalDate -> setCellValue(value).also { cellStyle = cellStyles.getStyle(DATE) }
        is LocalDateTime -> setCellValue(value).also { cellStyle = cellStyles.getStyle(DATETIME) }
        is LocalTime -> setCellValue(LocalDateTime.of(ZERO_DAY, value)).also { cellStyle = cellStyles.getStyle(TIME) }
        is RichTextString -> setCellValue(value)
        is String -> setCellValue(value)
        else -> throw UnsupportedCellTypeException()
    }
}

/**
 * Get a value of the [Cell] by the given [KClass]
 * @param kClass [KClass]
 * */
fun Cell.getCellValueAs(kClass: KClass<*>): Any? =
    when (kClass) {
        Boolean::class -> booleanCellValue
        Calendar::class -> dateCellValue?.asCalendar
        Date::class -> dateCellValue
        Double::class -> numericCellValue
        Int::class -> intCellValue()
        LocalDate::class -> dateCellValue?.asLocalDate
        LocalDateTime::class -> dateCellValue?.asLocalDateTime
        LocalTime::class -> dateCellValue?.asLocalTime
        RichTextString::class -> stringValue
        String::class -> stringValue
        else -> throw UnsupportedCellTypeException()
    }
