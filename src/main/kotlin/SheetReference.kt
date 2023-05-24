import annotations.Column
import exceptions.ColumnNotFoundException
import exceptions.MultipleKeyColumnException
import exceptions.NoKeyFieldException
import exceptions.NonUniqueColumnException
import exceptions.RowNotFoundException
import extensions.columnName
import extensions.fieldName
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
class SheetReference<T : Entity>(kClass: KClass<T>, val sheet: Sheet, val excelDB: ExcelDB) {
    /** Row index of the column names */
    val columnNameRowIndex: Int = kClass.findAnnotation<annotations.Sheet>()?.firstRowIndex ?: 0
    private val primaryConstructor = kClass.getPrimaryConstructor()
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
        keyField = mappedFields.filter { it.kParameter.findAnnotation<Column>()?.keyColumn == true }.getKeyField()
            ?: mappedFields.filter { it.kParameter.fieldName == "id" }.getKeyField()
    }

    private fun List<ParameterMapper>.getKeyField(): ParameterMapper? {
        if (size > 1)
            throw MultipleKeyColumnException(joinToString { it.kParameter.fieldName })
        return if (size == 1)
            first()
        else
            null
    }

    /**
     * Get a new [T] instance based on the data contained in the [Row]
     * @param row A [Row] from an Excel Sheet
     */
    fun getEntity(row: Row): T {
        return primaryConstructor.callBy(
            mappedFields.mapNotNull {
                val cellValue = row.getCell(it.columnIndex!!)?.let { cell -> it.getValue(cell, excelDB) }
                if (cellValue != null || it.kParameter.isRequired) {
                    it.kParameter to cellValue
                } else {
                    null
                }
            }.toMap()
        )
    }

    fun getEntity(rowIndex: Int): T = getEntity(getRow(rowIndex))

    fun getRow(rowIndex: Int): Row = sheet.getRow(rowIndex) ?: throw RowNotFoundException(rowIndex)

    fun getKeyType(): KClass<*> = keyField?.kParameter?.type?.classifier as KClass<*>

    fun getKeyCell(rowIndex: Int) = getKeyCell(getRow(rowIndex))
    fun getKeyCell(row: Row): Cell {
        if (keyField == null)
            throw NoKeyFieldException()
        return row.getCell(keyField.columnIndex!!)
    }

    private fun validateCells(
        cells: List<FieldMap>,
        kParameter: KParameter
    ) {
        if (cells.size > 1) throw NonUniqueColumnException(cells.joinToString { "${it.cell} -> ${it.fieldName}" })
        if (cells.isEmpty() && kParameter.isOptional.not()) {
            throw ColumnNotFoundException(kParameter.columnName.toString())
        }
    }

    fun getKeyCellIndex(): Int? = keyField?.columnIndex

    private class FieldMap(val fieldName: String, val cell: Cell)
}
