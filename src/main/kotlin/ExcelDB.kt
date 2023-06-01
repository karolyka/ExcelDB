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
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.reflect.KClass

/**
 * This class reads the data from an Excel Workbook
 *
 * @property fileName Name of the Excel workbook
 * @property fileMode Type of workbook creating - default: [FileMode.READ]
 */
class ExcelDB(private val fileName: String, private val fileMode: FileMode = FileMode.READ) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val workbook: Workbook
    private val cache = Cache()

    init {
        logger.debug { "File name: [$fileName], file mode: [$fileMode]" }
        ZipSecureFile.setMinInflateRatio(0.0)
        workbook = when (fileMode) {
            FileMode.READ -> readWorkbook()
            FileMode.CREATE -> createWorkbook()
            FileMode.READ_OR_CREATE -> readOrCreateWorkbook()
        }
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
     * Write data to Workbook
     * You have to call the [writeWorkbook] method to save to disk
     *
     * @param T         An [Entity]
     * @param kClass    [KClass] of the [Entity]
     * @param entity    An [Iterable] entity
     * @param sheetName Use this name for data when provided
     * */
    fun <T : Entity> writeDataToWorkbook(kClass: KClass<T>, entity: Iterable<T>, sheetName: String? = null) {
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
     * Write the Workbook to the disk
     *
     * @param fileName An optional filename. If it is `null` the [fileName] will be used.
     * */
    fun writeWorkbook(fileName: String = this.fileName) {
        logger.debug { "Write Excel workbook to [$fileName]" }
        if (workbook.numberOfSheets == 0) {
            throw NoDataException()
        }
        FileOutputStream(fileName).use {
            workbook.write(it)
            logger.debug { "Write Excel workbook to [$fileName] is done" }
        }
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

    private fun <T : Entity> writeDataToSheet(entity: Iterable<T>, worksheet: Sheet, fields: List<FieldReference<T>>) {
        entity.forEachIndexed { index, data ->
            cache.addEntity(data) { getEntityKeyMap(it, false) }
            worksheet.createRow(index + 1).let { row ->
                fields.forEachIndexed { column, fieldReference ->
                    fieldReference.property.get(data)?.let { value ->
                        if (fieldReference.isEntity) {
                            cache.addEntity(value as Entity) { getEntityKeyMap(it, true) }
                        } else {
                            value
                        }
                    }?.let {
                        row.createCell(column).setCellValue(it)
                    }
                }
            }
        }
    }

    private fun <T : Entity> getEntityKeyMap(kClass: KClass<T>, createAllowed: Boolean): MutableMap<Any?, Entity> =
        getData(kClass, createAllowed = createAllowed)
            .associateBy { cache.getKeyFieldReference(kClass).get(it) }
            .toMutableMap()
}
