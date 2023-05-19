import exceptions.UnsupportedDataTypeException
import extensions.asString
import extensions.intCellValue
import org.apache.poi.ss.usermodel.Cell
import java.time.LocalDateTime
import java.util.Date
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

/** Parameter mapper class to stores the parameter and the Excel column relation */
class ParameterMapper(val kParameter: KParameter, val columnIndex: Int?) {
    val getValue: Cell.() -> Any? = when (kParameter.type.javaType) {
        Boolean::class.java -> Cell::getBooleanCellValue
        Date::class.java -> Cell::getDateCellValue
        Double::class.java -> Cell::getNumericCellValue
        Int::class.java -> Cell::intCellValue
        LocalDateTime::class.java -> Cell::getLocalDateTimeCellValue
        String::class.java -> Cell::asString
        else -> throw UnsupportedDataTypeException(kParameter.name)
    }
}
