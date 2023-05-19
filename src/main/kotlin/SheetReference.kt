import annotations.Column
import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.PrimaryConstructorMissing
import exceptions.RowNotFoundException
import exceptions.UnsupportedParameterException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * This class stores the references for connect the [Entity] fields and the Excel columns
 *
 * @param kClass [KClass] of [Entity]
 * @property sheet  An Excel [Sheet] that contains data
 */
class SheetReference<T : Entity>(kClass: KClass<T>, val sheet: Sheet) {
    val columnNameRowIndex: Int = kClass.findAnnotation<annotations.Sheet>()?.firstRowIndex ?: 0
    private val primaryConstructor = kClass.primaryConstructor ?: throw PrimaryConstructorMissing(kClass.simpleName)
    private val fields: List<ParameterMapper>
    private val mappedFields: List<ParameterMapper>

    init {
        val fieldNamesRow = sheet.getRow(columnNameRowIndex)
            ?.map { FieldMap(it.stringCellValue.normalizeFieldName(), it) }
            ?: throw RowNotFoundException(columnNameRowIndex)
        fields = primaryConstructor.parameters.map { kParameter ->
            val cells = FieldName(kParameter.fieldName).let { fieldName ->
                fieldNamesRow.filter { fieldName.isEqual(it.fieldName) }
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

    private val KParameter.fieldName: String
        get() = findAnnotation<Column>()?.name
            ?: name
            ?: throw UnsupportedParameterException()

    private val KParameter.isRequired: Boolean
        get() = !isOptional

    private val KAnnotatedElement.columnName: String?
        get() = findAnnotation<Column>()?.name ?: (this as? KParameter)?.name

    private class FieldMap(val fieldName: String, val cell: Cell)

    private class FieldName(val name: String) {
        private val normalizedName by lazy { name.normalizeFieldName() }
        fun isEqual(fieldName: String): Boolean =
            name.equals(fieldName, true) ||
                normalizedName.equals(fieldName, true)
    }
}

private fun String.normalizeFieldName(): String {
    return this.lowercase().map {
        when (it) {
            in '0'..'9' -> it
            in 'a'..'z' -> it
            'é' -> 'e'
            'á' -> 'a'
            'í' -> 'i'
            in "öőó" -> 'o'
            in "üűú" -> 'u'
            in " _()[]" -> '_'
            else -> ""
        }
    }.joinToString("")
}
