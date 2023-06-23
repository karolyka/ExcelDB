package extensions

import Entity
import ExcelDB

/**
 * Get a [List]<[T]>
 *
 * @param T         An [Entity]
 * @param sheetName Use this name for data when provided
 */
inline fun <reified T : Entity> ExcelDB.getData(sheetName: String? = null): MutableList<T> {
    return getData(T::class, sheetName)
}

/**
 * Write data to Workbook
 * You have to call the [ExcelDB.writeWorkbook] method to save to disk
 *
 * @param T         An [Entity]
 * @param entity    An [Iterable] entity
 * @param sheetName Use this name for data when provided
 * */
inline fun <reified T : Entity> ExcelDB.writeDataToWorkbook(
    entity: Iterable<T>,
    sheetName: String? = null,
    clearCache: Boolean = false
) =
    writeDataToWorkbook(T::class, entity, sheetName, clearCache)
