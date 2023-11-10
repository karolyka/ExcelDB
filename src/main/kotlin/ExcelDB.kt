import exceptions.KeyNotFoundException
import exceptions.NoDataException
import exceptions.SheetNotFoundException
import extensions.createSheet
import extensions.getCellValueAs
import extensions.removeSheetIfExist
import extensions.setCellValue
import extensions.toList
import mu.KotlinLogging
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.reflect.KClass

private const val MIN_WIDTH = 200

/**
 * This class reads the data from an Excel Workbook
 *
 * @param fileName Name of the Excel workbook
 * @param fileMode Type of workbook creating - default: [FileMode.READ]
 */
@Suppress("TooManyFunctions")
class ExcelDB(private val fileName: String, private val fileMode: FileMode = FileMode.READ) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val workbook: Workbook
    private val cache = Cache()
    private val cellStyles: CellStyles

    /** Enable the auto-filter feature for the sheets */
    private var autoFilterEnabled = true

    /** Enable to calculate the widths for the columns */
    private var autoWidthEnabled = true

    init {
        logger.debug { "File name: [$fileName], file mode: [$fileMode]" }
        ZipSecureFile.setMinInflateRatio(0.0)
        workbook = when (fileMode) {
            FileMode.READ -> readWorkbook()
            FileMode.CREATE -> createWorkbook()
            FileMode.READ_OR_CREATE -> readOrCreateWorkbook()
        }
        cellStyles = CellStyles(workbook)
    }

    /**
     * Get a [List]<[T]>
     *
     * @param T         An [Entity]
     * @param kClass    [KClass] of the [Entity]
     * @param sheetName Use this name for data when provided
     * @param createAllowed Set `true` to allow creating a missing sheet
     */
    fun <T : Entity> getData(
        kClass: KClass<T>,
        sheetName: String? = null,
        createAllowed: Boolean = false
    ): MutableList<T> {
        logger.debug { "Get data for [$kClass]" }
        @Suppress("UNCHECKED_CAST")
        return cache.dataGetOrPut(kClass) { getIterator(kClass, sheetName, createAllowed).toList() } as MutableList<T>
    }

    /**
     * Get a [List]<[T]>
     *
     * @param T         An [Entity]
     * @param sheetName Use this name for data when provided
     */
    inline fun <reified T : Entity> getData(sheetName: String? = null): MutableList<T> = getData(T::class, sheetName)

    /**
     * Write data to Workbook
     * You have to call the [writeWorkbook] method to save to disk
     *
     * @param T          An [Entity]
     * @param kClass     [KClass] of the [Entity]
     * @param entity     An [Iterable] entity
     * @param sheetName  Use this name for data when provided
     * @param clearCache When `true` the cache will be cleared before write
     * */
    fun <T : Entity> writeDataToWorkbook(
        kClass: KClass<T>,
        entity: Iterable<T>,
        sheetName: String? = null,
        clearCache: Boolean = false
    ) {
        if (clearCache) {
            cache.clearData(kClass)
        }
        val dataSheetName = cache.sheetNameGetOrPut(kClass, sheetName)
        logger.debug { "Write [$kClass] data to [$dataSheetName] sheet" }
        with(workbook) {
            removeSheetIfExist(dataSheetName)
            createSheet(kClass, dataSheetName)
                .let { (sheet, fields) -> writeDataToSheet(entity, sheet, fields) }
        }
        cache.popRelatedEntity(kClass)?.let { entityKClass ->
            logger.debug { "Write related entity [$entityKClass]" }
            writeDataToWorkbook(entityKClass, getData(entityKClass))
        }
    }

    /**
     * Write data to Workbook
     * You have to call the [writeWorkbook] method to save to disk
     *
     * @param T         An [Entity]
     * @param entity    An [Iterable] entity
     * @param sheetName Use this name for data when provided
     * @param clearCache When `true` the cache will be cleared before write
     * */
    inline fun <reified T : Entity> writeDataToWorkbook(
        entity: Iterable<T>,
        sheetName: String? = null,
        clearCache: Boolean = false
    ) =
        writeDataToWorkbook(T::class, entity, sheetName, clearCache)

    /**
     * Write the Workbook to the disk
     *
     * @param fileName An optional filename. If it is `null` the [fileName] will be used.
     * */
    fun writeWorkbook(fileName: String = this.fileName) {
        logger.debug { "Write Excel workbook to [$fileName]" }
        if (workbook.numberOfSheets == 0) {
            throw NoDataException()
        }

        if (autoFilterEnabled || autoWidthEnabled) {
            workbook.sheetIterator().forEach { sheet ->
                val firstRow: XSSFRow by lazy { sheet.getRow(0) as XSSFRow }
                val lastCellNum by lazy { firstRow.lastCellNum.toInt() - 1 }
                val lastCell by lazy { firstRow.getCell(lastCellNum) }

                setupAutoFilterAndWidth(sheet, lastCell, lastCellNum)
            }
        }

        FileOutputStream(fileName).use {
            workbook.write(it)
            logger.debug { "Write Excel workbook to [$fileName] is done" }
        }
    }

    private fun setupAutoFilterAndWidth(
        sheet: Sheet,
        lastCell: XSSFCell,
        lastCellNum: Int
    ) {
        if (autoFilterEnabled) {
            sheet.setAutoFilter(CellRangeAddress(0, lastCell.rowIndex, 0, lastCellNum))
        }

        if (autoWidthEnabled) {
            for (it in 0..lastCellNum) {
                sheet.autoSizeColumn(it)
                if (sheet.getColumnWidth(it) == 0) {
                    sheet.setColumnWidth(it, MIN_WIDTH)
                }
            }
        }
    }

    /** Enable the auto-filter feature for the sheets */
    fun enableAutoFilter() {
        autoFilterEnabled = true
    }

    /** Disable the auto-filter feature for the sheets */
    fun disableAutoFilter() {
        autoFilterEnabled = false
    }

    /** Enable to calculate the widths for the columns */
    fun enableAutoWidth() {
        autoWidthEnabled = true
    }

    /** Disable to calculate the widths for the columns */
    fun disableAutoWidth() {
        autoWidthEnabled = false
    }

    /**
     * Find the nested data by the given [KClass] and [Cell] values
     *
     * @param T      An [Entity] type
     * @param kClass A class of [Entity]
     * @param cell   A [Cell]
     * @return [T] or `null` when the row not found
     * */
    internal fun <T : Entity> findEntity(kClass: KClass<T>, cell: Cell): T? {
        return cache.getKeyFieldReference(kClass).let { keyField ->
            cell.getCellValueAs(keyField.keyFieldKClass)?.let { key ->
                cache.getEntityOrNull(kClass, key) { getEntityKeyMap(kClass, false) }
                    ?: throw KeyNotFoundException(key.toString())
            }
        }
    }

    private fun <T : Entity> getIterator(
        kClass: KClass<T>,
        sheetName: String?,
        createAllowed: Boolean
    ): DataIterator<T> {
        logger.debug { "Get iterator for [$kClass]" }
        return cache.sheetNameGetOrPut(kClass, sheetName)
            .let {
                workbook.getSheet(it)
                    ?: if (createAllowed) workbook.createSheet(kClass, it).first else throw SheetNotFoundException(it)
            }
            .let { DataIterator(SheetReference(kClass, sheet = it, excelDB = this)) }
    }

    private fun createWorkbook(): XSSFWorkbook {
        logger.debug { "Creating an empty Excel workbook" }
        return XSSFWorkbook()
    }

    private fun readWorkbook(): Workbook {
        logger.debug { "Opening Excel workbook: [$fileName]" }
        return WorkbookFactory.create(FileInputStream(Paths.get(fileName).toFile()))
    }

    private fun readOrCreateWorkbook(): Workbook {
        return if (File(fileName).exists()) {
            readWorkbook()
        } else {
            createWorkbook()
        }
    }

    private fun <T : Entity> writeDataToSheet(
        entity: Iterable<T>,
        worksheet: Sheet,
        fields: List<FieldReference<T>>
    ) {
        entity.forEachIndexed { index, data ->
            cache.addEntity(data) { getEntityKeyMap(it, false, worksheet.sheetName) }
            worksheet.createRow(index + 1).let { row ->
                fields.forEachIndexed { column, fieldReference ->
                    fieldReference.property.get(data)?.let { value ->
                        if (fieldReference.isEntity) {
                            cache.addEntity(value as Entity) { getEntityKeyMap(it, true) }
                        } else {
                            value
                        }
                    }?.let {
                        row.createCell(column).setCellValue(it, cellStyles)
                    }
                }
            }
        }
    }

    private fun <T : Entity> getEntityKeyMap(
        kClass: KClass<T>,
        createAllowed: Boolean,
        sheetName: String? = null
    ): MutableMap<Any?, Entity> =
        getData(kClass, sheetName, createAllowed = createAllowed)
            .associateBy { cache.getKeyFieldReference(kClass).get(it) }
            .toMutableMap()
}
