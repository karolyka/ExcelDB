import extensions.asString

/**
 * This class implements an [Iterator] for [Entity] class and the related Excel Sheet
 *
 * @param T An [Entity] type
 * @param sheetReference A [SheetReference]
 */
class DataIterator<T : Entity>(private val sheetReference: SheetReference<T>) : Iterator<T> {
    private var rowIndex = sheetReference.columnNameRowIndex
    private val keyColumnIndex by lazy { sheetReference.getKeyCellIndex() ?: 0 }

    private fun hasNextRow(row: Int): Boolean {
        return sheetReference.sheet.getRow(row + 1)?.getCell(keyColumnIndex)?.asString()
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
