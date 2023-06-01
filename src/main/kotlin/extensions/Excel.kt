package extensions

import Entity
import FieldReference
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import kotlin.reflect.KClass

/**
 * Creates a new worksheet with the related fields
 *
 * @param T         An [Entity]
 * @param kClass    An [Entity] KClass
 * @param sheetName Name of the sheet
 * */
fun <T : Entity> Workbook.createSheet(
    kClass: KClass<T>,
    sheetName: String?
): Pair<Sheet, List<FieldReference<T>>> {
    return kClass.getFieldReferences().let { fields ->
        createSheet(sheetName).apply { createFieldNamesRow(fields) } to fields
    }
}

/**
 * Remove a sheet when it is exists
 *
 * @param sheetName Name of the sheet
 * */
fun Workbook.removeSheetIfExist(sheetName: String) {
    getSheet(sheetName)?.let { removeSheetAt(getSheetIndex(it)) }
}

private fun <T : Entity> Sheet.createFieldNamesRow(fields: List<FieldReference<T>>) {
    createRow(0).let { row ->
        fields.forEachIndexed { index, fieldReference ->
            row.createCell(index).setCellValue(fieldReference.name)
        }
    }
}
