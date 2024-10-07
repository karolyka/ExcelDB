import exceptions.UnsupportedDataTypeException
import extensions.asEntity
import extensions.asString
import extensions.getCellValueAs
import extensions.intCellValue
import org.apache.poi.ss.usermodel.Cell
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

/**
 * Parameter mapper class to stores the parameter and the Excel column relation
 *
 * @property kParameter  A parameter
 * @property columnIndex Index of Excel column
 * */
class ParameterMapper(val kParameter: KParameter, val columnIndex: Int?) {
    private val kClass by lazy { kParameter.asEntity ?: throw UnsupportedDataTypeException(kParameter.name) }

    /** Get a value of cell. The type of value depends on the type of the parameter */
    val getValue: (Cell, ExcelDB) -> Any? =
        when (kParameter.type.javaType) {
            Boolean::class.java -> { cell, _ -> cell.booleanCellValue }
            Calendar::class.java -> { cell, _ -> cell.getCellValueAs(Calendar::class) }
            Date::class.java -> { cell, _ -> cell.dateCellValue }
            Double::class.java -> { cell, _ -> cell.numericCellValue }
            Int::class.java,
            Integer::class.java,
            -> { cell, _ -> cell.intCellValue() }
            LocalDate::class.java -> { cell, _ -> cell.getCellValueAs(LocalDate::class) }
            LocalDateTime::class.java -> { cell, _ -> cell.localDateTimeCellValue }
            LocalTime::class.java -> { cell, _ -> cell.getCellValueAs(LocalTime::class) }
            String::class.java -> { cell, _ -> cell.asString() }
            else -> { cell, excelDb -> excelDb.findEntity(kClass, cell) }
        }
}
