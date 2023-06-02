import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.NullValueException
import exceptions.PrimaryConstructorMissing
import exceptions.RowNotFoundException
import extensions.columnName
import extensions.fieldName
import extensions.getKeyField
import extensions.getPrimaryConstructor
import extensions.isIdColumn
import extensions.isKeyColumn
import extensions.isRequired
import extensions.normalizeFieldName
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation

/**
 * This class stores the references for connect the [Entity] fields and the Excel columns
 *
 * @param T An [Entity] type
 * @param kClass [KClass] of [Entity]
 * @property sheet  An Excel [Sheet] that contains data
 * @property excelDB An [ExcelDB]
 */
class SheetReference<T : Entity>(kClass: KClass<T>, val sheet: Sheet, val excelDB: ExcelDB) {
    /** Row index of the column names */
    val columnNameRowIndex: Int = kClass.findAnnotation<annotations.Sheet>()?.firstRowIndex ?: 0
    private val primaryConstructor = kClass.getPrimaryConstructor().validateVisibility(kClass)
    private val fields: List<ParameterMapper>
    private val mappedFields: List<ParameterMapper>
    private val keyField: ParameterMapper?

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
        keyField = with(mappedFields) {
            filter { it.kParameter.isKeyColumn }.getKeyField()
                ?: filter { it.kParameter.isIdColumn }.getKeyField()
        }
    }

    private fun getEntity(row: Row): T {
        return primaryConstructor.callBy(
            mappedFields.mapNotNull {
                val cellValue = row.getCell(it.columnIndex!!)?.let { cell -> it.getValue(cell, excelDB) }
                if (cellValue != null || it.kParameter.isRequired) {
                    if (cellValue == null && it.kParameter.type.isMarkedNullable.not()) {
                        throw NullValueException(it.kParameter.fieldName)
                    }
                    it.kParameter to cellValue
                } else {
                    null
                }
            }.toMap()
        )
    }

    /**
     * Get a new [T] instance based on the data contained in the [Row]
     * @param rowIndex An index of a [Row]
     */
    fun getEntity(rowIndex: Int): T = getEntity(getRow(rowIndex))

    private fun getRow(rowIndex: Int): Row = sheet.getRow(rowIndex) ?: throw RowNotFoundException(rowIndex)

    /** This method provides the index of key cell for nested data */
    fun getKeyCellIndex(): Int? = keyField?.columnIndex

    private fun validateCells(
        cells: List<FieldMap>,
        kParameter: KParameter
    ) {
        if (cells.size > 1) throw NonUniqueColumnException(cells.joinToString { "${it.cell} -> ${it.fieldName}" })
        if (cells.isEmpty() && kParameter.isOptional.not()) {
            throw ColumnNotFoundException(kParameter.columnName.toString())
        }
    }

    private class FieldMap(val fieldName: String, val cell: Cell)
}

private fun <R> KFunction<R>.validateVisibility(kClass: KClass<*>): KFunction<R> {
    if (visibility != KVisibility.PUBLIC) {
        throw PrimaryConstructorMissing(kClass.qualifiedName)
    }
    return this
}
