import extensions.asString
import extensions.intCellValue
import org.apache.poi.ss.usermodel.Cell
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

/** Parameter mapper class to stores the parameter and the Excel column relation */
class ParameterMapper(val kParameter: KParameter, val columnIndex: Int?) {
    val getValue: Cell.() -> Any? = when (kParameter.type.javaType) {
        Int::class.java -> Cell::intCellValue
        String::class.java -> Cell::asString
        Double::class.java -> Cell::getNumericCellValue
        else -> Cell::getStringCellValue
    }
}
