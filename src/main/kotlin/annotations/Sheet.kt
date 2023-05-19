package annotations

/** An annotation to set up an entity properties

 * @property name          Sheet name in the Excel workbook
 * @property firstRowIndex First row of sheet, that contains the column names
 */
annotation class Sheet(val name: String = "", val firstRowIndex: Int = 0)
