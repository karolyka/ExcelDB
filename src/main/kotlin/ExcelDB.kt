import annotations.Sheet
import exceptions.NoDataException
import exceptions.SheetNotFoundException
import extensions.getPrimaryConstructor
import extensions.setCellValue
import mu.KotlinLogging
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * This class reads the data from an Excel Workbook
 *
 * @property fileName Name of the Excel workbook
 * @property fileMode Type of workbook creating - default: [FileMode.READ]
 */
@Suppress("TooManyFunctions")
class ExcelDB(private val fileName: String, private val fileMode: FileMode = FileMode.READ) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val workbook: Workbook

    init {
        logger.debug { "File name: [$fileName], file mode: [$fileMode]" }
        ZipSecureFile.setMinInflateRatio(0.0)
        workbook = when (fileMode) {
            FileMode.READ -> readWorkbook()
            FileMode.CREATE -> createWorkbook()
            FileMode.READ_OR_CREATE -> readOrCreateWorkbook()
        }
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

    /**
     * Get an [Iterator] for an [Entity]
     *
     * @param T An [Entity]
     * @param sheetName Use this name for data when provided
     */
    inline fun <reified T : Entity> getIterator(sheetName: String? = null): Iterator<T> {
        return getIterator(T::class, sheetName)
    }

    /**
     * Get an [Iterator] for an [Entity]
     *
     * @param T An [Entity]
     * @param kClass    [KClass] of the [Entity]
     * @param sheetName Use this name for data when provided
     */
    fun <T : Entity> getIterator(kClass: KClass<T>, sheetName: String? = null): Iterator<T> {
        return getSheetName(kClass, sheetName)
            .let { workbook.getSheet(it) ?: throw SheetNotFoundException(it) }
            .let { DataIterator(SheetReference(kClass, sheet = it)) }
    }

    /**
     * Get a [List]<[T]>
     *
     * @param T An [Entity]
     * @param sheetName Use this name for data when provided
     */
    inline fun <reified T : Entity> getData(sheetName: String? = null): List<T> {
        return getData(T::class, sheetName)
    }

    /**
     * Get a [List]<[T]>
     *
     * @param T An [Entity]
     * @param kClass    [KClass] of the [Entity]
     * @param sheetName Use this name for data when provided
     */
    fun <T : Entity> getData(kClass: KClass<T>, sheetName: String? = null): List<T> {
        val iterator = getIterator(kClass, sheetName)
        val data = mutableListOf<T>()
        while (iterator.hasNext()) {
            data.add(iterator.next())
        }
        return data
    }

    /**
     * Write data to Workbook
     * You have to call the [writeExcel] method to save to disk
     *
     * @param T An [Entity]
     * @param kClass    [KClass] of the [Entity]
     * @param entity    An [Iterable] entity
     * @param sheetName Use this name for data when provided
     * */
    fun <T : Entity> writeData(kClass: KClass<T>, entity: Iterable<T>, sheetName: String? = null) {
        val dataSheetName = getSheetName(kClass, sheetName)
        with(workbook) {
            removeSheetIfExist(dataSheetName)
            createSheet(dataSheetName).let { sheet ->
                kClass.getPrimaryConstructor()
                    .parameters.map { kParameter -> FieldReference(kClass, kParameter) }.let { fields ->
                        createFieldNamesRow(sheet, fields)
                        writeDataToSheet(entity, sheet, fields)
                    }
            }
        }
    }

    /**
     * Write the Workbook to the disk
     *
     * @param fileName An optional filename. If it is `null` the [fileName] will be used.
     * */
    fun writeExcel(fileName: String = this.fileName) {
        logger.debug { "Write Excel workbook to [$fileName]" }
        if (workbook.numberOfSheets == 0) {
            throw NoDataException()
        }
        FileOutputStream(fileName).use {
            workbook.write(it)
            logger.debug { "Write Excel workbook to [$fileName] is done" }
        }
    }

    private fun Workbook.removeSheetIfExist(dataSheetName: String) {
        getSheet(dataSheetName)?.let {
            removeSheetAt(getSheetIndex(it))
        }
    }

    private fun <T : Entity> writeDataToSheet(
        entity: Iterable<T>,
        sheet: org.apache.poi.ss.usermodel.Sheet,
        fields: List<FieldReference<T>>
    ) {
        entity.forEachIndexed { index, data ->
            sheet.createRow(index + 1).let { row ->
                fields.forEachIndexed { column, fieldReference ->
                    row.createCell(column).setCellValue(fieldReference.property.get(data))
                }
            }
        }
    }

    private fun <T : Entity> createFieldNamesRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        fields: List<FieldReference<T>>
    ) {
        sheet.createRow(0).let { row ->
            fields.forEachIndexed { index, fieldReference ->
                row.createCell(index).setCellValue(fieldReference.name)
            }
        }
    }

    private fun <T : Entity> getSheetName(kClass: KClass<T>, sheetName: String?) = (
        sheetName
            ?: kClass.findAnnotation<Sheet>()?.name?.takeIf { it.isNotBlank() }
            ?: kClass.simpleName.toString()
        )
}
