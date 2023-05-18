import annotations.Sheet
import exceptions.SheetNotFoundException
import mu.KotlinLogging
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * This class reads the data from an Excel Workbook
 *
 * @property fileName Name of the Excel workbook
 * */
class ExcelDB(private val fileName: String) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val workbook: Workbook

    init {
        ZipSecureFile.setMinInflateRatio(0.0)
        logger.debug { "Opening Excel workbook: [$fileName]" }
        workbook = WorkbookFactory.create(FileInputStream(Paths.get(fileName).toFile()))
    }

    /**
     * Get an [Iterator] for an [Entity]
     *
     * @param sheetName Use this name for data when provided
     * */
    inline fun <reified T : Entity> getIterator(sheetName: String? = null): Iterator<T> {
        return getIterator(T::class, sheetName)
    }

    /**
     * Get an [Iterator] for an [Entity]
     *
     * @param kClass    [KClass] of the [Entity]
     * @param sheetName Use this name for data when provided
     * */
    fun <T : Entity> getIterator(kClass: KClass<T>, sheetName: String? = null): Iterator<T> {
        return (
            sheetName
                ?: kClass.findAnnotation<Sheet>()?.name?.takeIf { it.isNotBlank() }
                ?: kClass.simpleName.toString()
            )
            .let { workbook.getSheet(it) ?: throw SheetNotFoundException(it) }
            .let { SheetIterator(SheetReference(kClass, sheet = it)) }
    }
}
