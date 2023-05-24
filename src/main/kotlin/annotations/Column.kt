package annotations

/** An annotation to set up a column properties
 *
 * @property name Column name in the Excel sheet */
annotation class Column(val name: String = "", val keyColumn: Boolean = false)
