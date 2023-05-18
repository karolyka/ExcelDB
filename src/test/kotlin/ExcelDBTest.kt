import org.junit.jupiter.api.assertDoesNotThrow
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ExcelDBTest {

    @Test
    fun `verify ExcelDB throws exception when the file doesn't exists`() {
        assertFailsWith<FileNotFoundException> {
            ExcelDB("invalid_filename")
        }
    }

    @Test
    fun `verify ExcelDB doesn't throw an exception when the file exists`() {
        assertDoesNotThrow {
            ExcelDB(TEST_WORKBOOK)
        }
    }

}