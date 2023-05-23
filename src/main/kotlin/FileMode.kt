/** Workbook */
enum class FileMode {
    /** Read an existing Workbook */
    READ,

    /** Create a new Workbook */
    CREATE,

    /** Read an existing Workbook or create if not */
    READ_OR_CREATE
}
