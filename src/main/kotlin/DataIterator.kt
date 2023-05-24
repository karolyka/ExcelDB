import extensions.asString
import extensions.getCellValueAs
import org.apache.poi.ss.usermodel.Cell

/**
 * This class implements the [Iterator] for [Entity] class and the related Excel Sheet
 *
 * @param T  An [Entity] type
 * @property sheetReference
 */
class DataIterator<T : Entity>(private val sheetReference: SheetReference<T>) : Iterator<T> {
    private var rowIndex = sheetReference.columnNameRowIndex
    private val indexColumn by lazy { sheetReference.getKeyCellIndex() ?: 0 }
    private val keyType by lazy { sheetReference.getKeyType() }

    fun find(cell: Cell): T? {
        val value = cell.getCellValueAs(keyType)
        if (value != null) {
            var index = sheetReference.columnNameRowIndex
            while (hasNextRow(index)) {
                if (value == sheetReference.getKeyCell(++index).getCellValueAs(keyType)) {
                    return sheetReference.getEntity(index)
                }
            }
        }
        return null
    }

    private fun hasNextRow(row: Int): Boolean {
        return sheetReference.sheet.getRow(row + 1)?.getCell(indexColumn)?.asString()
            .let { !it.isNullOrBlank() }
    }

    override fun hasNext(): Boolean = hasNextRow(rowIndex)

    override fun next(): T {
        if (hasNext().not()) {
            throw NoSuchElementException()
        }
        return sheetReference.getEntity(++rowIndex)
    }
}
