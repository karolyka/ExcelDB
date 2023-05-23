import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.RowNotFoundException
import extensions.columnName
import extensions.getPrimaryConstructor
import extensions.isRequired
import extensions.normalizeFieldName
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

/**
 * This class stores the references for connect the [Entity] fields and the Excel columns
 *
 * @param T An [Entity] type
 * @param kClass [KClass] of [Entity]
 * @property sheet  An Excel [Sheet] that contains data
 */
class SheetReference<T : Entity>(kClass: KClass<T>, val sheet: Sheet) {
    /** Row index of the column names */
    val columnNameRowIndex: Int = kClass.findAnnotation<annotations.Sheet>()?.firstRowIndex ?: 0
    private val primaryConstructor = kClass.getPrimaryConstructor()
    private val fields: List<ParameterMapper>
    private val mappedFields: List<ParameterMapper>

    init {
        val fieldNamesRow = sheet.getRow(columnNameRowIndex)
            ?.map { FieldMap(it.stringCellValue.normalizeFieldName(), it) }
            ?: throw RowNotFoundException(columnNameRowIndex)
        fields = primaryConstructor.parameters.map { kParameter ->
            val cells = FieldReference(kClass, kParameter).let { fieldReference ->
                fieldNamesRow.filter { fieldReference.isEqual(it.fieldName) }
            }
            validateCells(cells, kParameter)
            ParameterMapper(kParameter, cells.firstOrNull()?.cell?.columnIndex)
        }
        mappedFields = fields.filter { it.columnIndex != null }
    }

    /**
     * Get a new [T] instance based on the data contained in the [Row]
     * @param row A [Row] from an Excel Sheet
     */
    fun getEntity(row: Row): T {
        return primaryConstructor.callBy(
            mappedFields.mapNotNull {
                val cellValue = row.getCell(it.columnIndex!!)?.let { cell -> it.getValue(cell) }
                if (cellValue != null || it.kParameter.isRequired) {
                    it.kParameter to cellValue
                } else {
                    null
                }
            }.toMap()
        )
    }

    private fun validateCells(
        cells: List<FieldMap>,
        kParameter: KParameter
    ) {
        if (cells.size > 1) throw NonUniqueColumnException(cells.joinToString { it.fieldName })
        if (cells.isEmpty() && kParameter.isOptional.not()) {
            throw ColumnNotFoundException(kParameter.columnName.toString())
        }
    }

    private class FieldMap(val fieldName: String, val cell: Cell)
}
