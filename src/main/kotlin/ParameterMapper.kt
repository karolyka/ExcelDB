import exceptions.UnsupportedDataTypeException
import extensions.asString
import extensions.intCellValue
import org.apache.poi.ss.usermodel.Cell
import java.time.LocalDateTime
import java.util.Date
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaType

/**
 * Parameter mapper class to stores the parameter and the Excel column relation
 *
 * @property kParameter  A parameter
 * @property columnIndex Index of Excel column
 * */
class ParameterMapper(val kParameter: KParameter, val columnIndex: Int?) {

    /** Get a value of cell. The type of value depends on the type of the parameter */
    val getValue: (Cell, ExcelDB) -> Any? = when (kParameter.type.javaType) {
        Boolean::class.java -> { cell, _ -> cell.booleanCellValue }
        Date::class.java -> { cell, _ -> cell.dateCellValue }
        Double::class.java -> { cell, _ -> cell.numericCellValue }
        Int::class.java -> { cell, _ -> cell.intCellValue() }
        LocalDateTime::class.java -> { cell, _ -> cell.localDateTimeCellValue }
        String::class.java -> { cell, _ -> cell.asString() }
        else -> { cell, excelDb ->
            (kParameter.type.classifier as KClass<*>).let { kClass ->
                if (kClass.isSubclassOf(Entity::class)) {
                    @Suppress("UNCHECKED_CAST")
                    kClass as KClass<Entity>
                    if (dataIterator == null) {
                        dataIterator = excelDb.getIterator(kClass)
                    }
                    dataIterator!!.find(cell)
                } else {
                    throw UnsupportedDataTypeException(kParameter.name)
                }
            }
        }
    }

    private var dataIterator: DataIterator<Entity>? = null
}
