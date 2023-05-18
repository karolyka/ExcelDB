import exceptions.RowNotFoundException
import extensions.asString

/**
 * This class implements the [Iterator] for [Entity] class and the related Excel Sheet
 *
 * @property sheetReference
 * */
class SheetIterator<T : Entity>(private val sheetReference: SheetReference<T>) : Iterator<T> {
    private var rowIndex = sheetReference.columnNameRowIndex
    override fun hasNext(): Boolean {
        return sheetReference.sheet.getRow(rowIndex + 1)?.getCell(1)?.asString()
            .let { !it.isNullOrBlank() }
    }

    override fun next(): T {
        if (hasNext().not())
            throw NoSuchElementException()
        val row = sheetReference.sheet.getRow(++rowIndex) ?: throw RowNotFoundException(rowIndex)
        return sheetReference.getEntity(row)
    }
}