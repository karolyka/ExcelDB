import domain.DataTypes
import domain.user.User
import exceptions.NoDataException
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExcelDBWriteDataTest {
    private val excelDB: ExcelDB = ExcelDB(TEST_WORKBOOK_FOR_WRITE, FileMode.CREATE)

    @BeforeTest
    fun `remove temporary Excel file before testing`() {
        removeTemporaryExcelFile()
    }

    @AfterTest
    fun `remove temporary Excel file after testing`() {
        removeTemporaryExcelFile()
    }

    private fun removeTemporaryExcelFile() {
        File(TEST_WORKBOOK_FOR_WRITE).run {
            if (exists()) {
                delete()
            }
        }
    }

    @Test
    fun `verify writeData thrown an exception when there is no data`() {
        assertFailsWith<NoDataException> {
            excelDB.writeWorkbook()
        }
    }

    @Test
    fun `verify writeData saves all records`() {
        assertDoesNotThrow {
            excelDB.writeDataToWorkbook(User::class, USERS)
            excelDB.writeWorkbook()

            assertTrue { File(TEST_WORKBOOK_FOR_WRITE).exists() }
            assertEquals(USERS, ExcelDB(TEST_WORKBOOK_FOR_WRITE).getData<User>())
        }
    }

    @Test
    fun `verify writeData thrown an exception when filename is invalid`() {
        assertFailsWith<FileNotFoundException> {
            ExcelDB("@/invalid file name", FileMode.CREATE).run {
                writeDataToWorkbook(User::class, USERS)
                writeWorkbook()
            }
        }
    }

    @Test
    fun `verify writeData doesn't throw with read or create filemode`() {
        assertDoesNotThrow {
            ExcelDB(TEST_WORKBOOK_FOR_WRITE, FileMode.READ_OR_CREATE).run {
                writeDataToWorkbook(User::class, USERS)
                writeWorkbook()

                assertTrue { File(TEST_WORKBOOK_FOR_WRITE).exists() }
                assertEquals(USERS, ExcelDB(TEST_WORKBOOK_FOR_WRITE, FileMode.READ_OR_CREATE).getData<User>())
            }
        }
    }

    @Test
    fun `verify writeData saves all records with overwrite the existing sheet`() {
        assertDoesNotThrow {
            val testUsers = USERS + listOf(User(100, "Hundred"))

            excelDB.writeDataToWorkbook(User::class, USERS)
            excelDB.writeWorkbook()

            ExcelDB(TEST_WORKBOOK_FOR_WRITE, FileMode.READ_OR_CREATE).run {
                writeDataToWorkbook(User::class, testUsers)
                writeWorkbook()

                assertTrue { File(TEST_WORKBOOK_FOR_WRITE).exists() }
                assertEquals(testUsers, ExcelDB(TEST_WORKBOOK_FOR_WRITE, FileMode.READ_OR_CREATE).getData<User>())
            }
        }
    }

    @Test
    fun `verify writeData saves all records with all supported type`() {
        assertDoesNotThrow {
            excelDB.writeDataToWorkbook(DataTypes::class, DATA_TYPES)
            excelDB.writeWorkbook()

            assertTrue { File(TEST_WORKBOOK_FOR_WRITE).exists() }
            assertEquals(DATA_TYPES, ExcelDB(TEST_WORKBOOK_FOR_WRITE).getData<DataTypes>())
        }
    }
}
