import annotations.Column
import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.PrimaryConstructorMissing
import exceptions.RowNotFoundException
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
 * @param sheet  An Excel sheet that contains data
 * */
class SheetReference<T : Entity>(kClass: KClass<T>, val sheet: Sheet) {
    val columnNameRowIndex: Int = kClass.findAnnotation<annotations.Sheet>()?.firstRowIndex ?: 0
    private val primaryConstructor = kClass.primaryConstructor ?: throw PrimaryConstructorMissing(kClass.simpleName)
    private val fields: List<ParameterMapper>
    private val mappedFields: List<ParameterMapper>

    init {
        val parameters = primaryConstructor.parameters
        val fieldNamesRow = sheet.getRow(columnNameRowIndex)?.map { it.stringCellValue.stripFieldName() to it }
            ?: throw RowNotFoundException(columnNameRowIndex)
        fields = parameters.map { kParameter ->
            val cells = fieldNamesRow.filter { isColumn(kParameter, it.first) }
            validateCells(cells, kParameter)
            ParameterMapper(kParameter, cells.firstOrNull()?.second?.columnIndex)
        }
        mappedFields = fields.filter { it.columnIndex != null }
    }

    private fun validateCells(
        cells: List<Pair<String, Cell>>,
        kParameter: KParameter
    ) {
        if (cells.size > 1) throw NonUniqueColumnException(cells.joinToString { it.first })
        if (cells.isEmpty() && kParameter.isOptional.not()) {
            throw ColumnNotFoundException(kParameter.columnName.toString())
        }
    }

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

    private val KParameter.isRequired: Boolean
        get() = !isOptional

    private val KAnnotatedElement.columnName: String?
        get() = findAnnotation<Column>()?.name ?: (this as? KParameter)?.name

    private fun isColumn(kParameter: KParameter, columnName: String) =
        isColumn(kParameter, kParameter.name!!, columnName)

    private fun isColumn(kAnnotatedElement: KAnnotatedElement, name: String, columnName: String) =
        (kAnnotatedElement.columnName ?: name).let {
            it.equals(columnName, true) || it.stripFieldName().equals(columnName, true)
        }

    private fun String.stripFieldName(): String {
        return this.lowercase().map {
            when (it) {
                in setOf('ö', 'ő', 'ó') -> 'o'
                in setOf('ü', 'ű', 'ú') -> 'u'
                'é' -> 'e'
                'á' -> 'a'
                'í' -> 'i'
                in '0'..'9' -> it
                in 'a'..'z' -> it
                in setOf('_', '(', ')', '[', ']') -> '_'
                else -> ""
            }
        }.joinToString("")
    }
}
