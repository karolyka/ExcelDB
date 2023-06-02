package annotations

/** An annotation to set up a column properties
 *
 * @property name      Column name in the Excel sheet
 * @property keyColumn `true` means this column is the key for nested data
 * */
annotation class Column(val name: String = "", val keyColumn: Boolean = false)
