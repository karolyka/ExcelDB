import domain.BoolAndFormula
import domain.Missing
import domain.SpecialCharacters
import domain.User
import domain.UserMissingColumn
import domain.UserNonUniqueColumns
import domain.UserWithDefault
import domain.UserWithOptional
import domain.UserWithSheetAnnotation
import domain.UserWithUnsupportedField
import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.SheetNotFoundException
import exceptions.UnsupportedDataTypeException
import org.junit.jupiter.api.assertDoesNotThrow
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataIteratorTest {
    private val excelDB: ExcelDB = ExcelDB(TEST_WORKBOOK)

    @Test
    fun `verify iterator throws exception when the sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getIterator<Missing>()
        }
    }

    @Test
    fun `verify iterator throws exception when the provided sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getIterator<User>("Missing")
        }
    }

    @Test
    fun `verify iterator doesn't throw an exception when the sheet exists`() {
        assertDoesNotThrow {
            excelDB.getIterator<User>()
        }
    }

    @Test
    fun `verify iterator throws exception when a required column is missing`() {
        assertFailsWith<ColumnNotFoundException> {
            excelDB.getIterator<UserMissingColumn>()
        }
    }

    @Test
    fun `verify iterator throws exception when a column mapping is not unique`() {
        assertFailsWith<NonUniqueColumnException> {
            excelDB.getIterator<UserNonUniqueColumns>()
        }
    }

    @Test
    fun `verify iterator doesn't throws exception when an optional column is missing`() {
        assertDoesNotThrow {
            excelDB.getIterator<UserWithOptional>()
        }
    }

    @Test
    fun `verify iterator doesn't throws exception when an sheet annotation has valid name`() {
        assertDoesNotThrow {
            excelDB.getIterator<UserWithSheetAnnotation>()
        }
    }

    @Test
    fun `verify iterator hasNext returns true when sheet has data`() {
        assertTrue { excelDB.getIterator<User>().hasNext() }
    }

    @Test
    fun `verify iterator hasNext returns false when sheet has no data`() {
        assertFalse { excelDB.getIterator<User>("EmptyUser").hasNext() }
    }

    @Test
    fun `verify iterator next throws exception when there is no next data row`() {
        assertFailsWith<NoSuchElementException> {
            excelDB.getIterator<User>("EmptyUser").next()
        }
    }

    @Test
    fun `verify iterator next throws exception when a field type is unsupported`() {
        assertFailsWith<UnsupportedDataTypeException> {
            excelDB.getIterator<UserWithUnsupportedField>().next()
        }
    }

    @Test
    fun `verify iterator next returns valid data`() {
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
    fun `verify iterator next returns valid data with default values`() {
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

    @Test
    fun `verify to read bool and formula types cells`() {
        val boolAndFormulas = listOf(
            BoolAndFormula(false, 2, "A", LocalDateTime.of(2023, 4, 27, 0, 0), "2023-04-28".toDate()),
            BoolAndFormula(true, 6, "AB", LocalDateTime.of(2023, 5, 1, 13, 4, 15), "2023-05-02 14:05:16".toDateTime()),
            BoolAndFormula(true, 8, "ab", LocalDateTime.of(2023, 5, 2, 13, 4, 15), "2023-07-02 00:00:00".toDateTime())
        )
        val boolAndFormulaIterator = excelDB.getIterator<BoolAndFormula>()
        boolAndFormulas.forEach {
            assertEquals(it, boolAndFormulaIterator.next())
        }
    }

    @Test
    fun `verify to normalize the column name when it contains special character`() {
        val specialCharacters = listOf(
            SpecialCharacters(1, "col1", "col2", "col3")
        )
        val specialCharactersIterator = excelDB.getIterator<SpecialCharacters>()
        specialCharacters.forEach {
            assertEquals(it, specialCharactersIterator.next())
        }
    }

    private fun String.toDate(): Date = SimpleDateFormat("yyyy-MM-dd").parse(this)
    private fun String.toDateTime(): Date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(this)
}
