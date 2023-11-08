import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Workbook

private const val DATE_FORMAT = "yyyy-mm-dd"
private const val TIME_FORMAT = "hh:MM:ss"

/** This class holds the style related information for a cell and [Workbook] */
class CellStyles(workbook: Workbook) {

    /** This enum class represents some date and time related styles */
    enum class Style(internal val formatString: String) {
        DATE(DATE_FORMAT),
        DATETIME("$DATE_FORMAT $TIME_FORMAT"),
        TIME(TIME_FORMAT)
    }

    private val styleMap = mutableMapOf<Style, CellStyle>()

    init {
        val creationHelper = workbook.creationHelper
        Style.values().forEach { styleMap[it] = getStyle(workbook, creationHelper, it.formatString) }
    }

    /** Get a [CellStyle] for the given [Style] parameter */
    fun getStyle(style: Style): CellStyle = styleMap[style]!!

    private fun getStyle(
        workbook: Workbook,
        creationHelper: CreationHelper,
        formatString: String
    ): CellStyle {
        return workbook.createCellStyle().apply {
            dataFormat = creationHelper.createDataFormat().getFormat(formatString)
        }
    }
}
