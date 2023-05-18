import domain.Missing
import domain.User
import domain.UserMissingColumn
import domain.UserNonUniqueColumns
import domain.UserWithDefault
import domain.UserWithOptional
import domain.UserWithSheetAnnotation
import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.SheetNotFoundException
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetIteratorTest {
    private val excelDB: ExcelDB = ExcelDB(TEST_WORKBOOK)

    @Test
    fun `verify getIterator throws exception when the sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getIterator<Missing>()
        }
    }

    @Test
    fun `verify getIterator throws exception when the provided sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getIterator<User>("Missing")
        }
    }

    @Test
    fun `verify getIterator doesn't throw an exception when the sheet exists`() {
        assertDoesNotThrow {
            excelDB.getIterator<User>()
        }
    }

    @Test
    fun `verify getIterator throws exception when a required column is missing`() {
        assertFailsWith<ColumnNotFoundException> {
            excelDB.getIterator<UserMissingColumn>()
        }
    }

    @Test
    fun `verify getIterator throws exception when a column mapping is not unique`() {
        assertFailsWith<NonUniqueColumnException> {
            excelDB.getIterator<UserNonUniqueColumns>()
        }
    }

    @Test
    fun `verify getIterator doesn't throws exception when an optional column is missing`() {
        assertDoesNotThrow {
            excelDB.getIterator<UserWithOptional>()
        }
    }

    @Test
    fun `verify getIterator doesn't throws exception when an sheet annotation has valid name`() {
        assertDoesNotThrow {
            excelDB.getIterator<UserWithSheetAnnotation>()
        }
    }

    @Test
    fun `verify getIterator hasNext returns true when sheet has data`() {
        assertTrue { excelDB.getIterator<User>().hasNext() }
    }

    @Test
    fun `verify getIterator hasNext returns false when sheet has no data`() {
        assertFalse { excelDB.getIterator<User>("EmptyUser").hasNext() }
    }

    @Test
    fun `verify getIterator next returns valid data`() {
        val users = listOf(
            User(1, "One"),
            User(2, "Two"),
            User(3, "Three")
        )
        val userIterator = excelDB.getIterator<User>()
        users.forEach {
            assertTrue { userIterator.hasNext() }
            assertEquals(it, userIterator.next())
        }
        assertFalse(userIterator.hasNext())
    }

    @Test
    fun `verify getIterator next returns valid data with default values`() {
        val users = listOf(
            UserWithDefault(1, "One", "one@one.one"),
            UserWithDefault(2, "Two"),
            UserWithDefault(3, "Three")
        )
        val userIterator = excelDB.getIterator<UserWithDefault>()
        users.forEach {
            assertTrue { userIterator.hasNext() }
            assertEquals(it, userIterator.next())
        }
        assertFalse(userIterator.hasNext())
    }
}
