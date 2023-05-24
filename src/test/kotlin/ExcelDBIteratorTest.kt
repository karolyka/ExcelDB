import domain.BoolAndFormula
import domain.Missing
import domain.SpecialCharacters
import domain.User
import domain.UserMissingColumn
import domain.UserNonUniqueColumns
import domain.UserWithAnnotations
import domain.UserWithDefault
import domain.UserWithOptional
import domain.UserWithSheetAnnotation
import domain.UserWithUnsupportedField
import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.RowNotFoundException
import exceptions.SheetNotFoundException
import exceptions.UnsupportedDataTypeException
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExcelDBIteratorTest {
    private val excelDB: ExcelDB = ExcelDB(TEST_WORKBOOK)

    @Test
    fun `verify iterator throws exception when the sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getIterator<Missing>()
        }
    }

    @Test
    fun `verify iterator throws exception when the sheet is empty`() {
        assertFailsWith<RowNotFoundException> {
            excelDB.getIterator<User>("EmptySheet")
        }
    }

    @Test
    fun `verify iterator throws exception when the provided sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getIterator<User>(MISSING)
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
    fun `verify iterator doesn't throws exception when an sheet annotation has valid name with class parameter`() {
        assertDoesNotThrow {
            excelDB.getIterator(User::class)
        }
    }

    @Test
    fun `verify iterator hasNext returns false when sheet has no data with class parameter`() {
        assertFalse { excelDB.getIterator(User::class, EMPTY_USER).hasNext() }
    }

    @Test
    fun `verify iterator hasNext returns false when sheet has no data`() {
        assertFalse { excelDB.getIterator<User>(EMPTY_USER).hasNext() }
    }

    @Test
    fun `verify iterator next throws exception when there is no next data row`() {
        assertFailsWith<NoSuchElementException> {
            excelDB.getIterator<User>(EMPTY_USER).next()
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
        val userIterator = excelDB.getIterator<User>()
        USERS.forEach {
            assertTrue { userIterator.hasNext() }
            assertEquals(it, userIterator.next())
        }
        assertFalse(userIterator.hasNext())
    }

    @Test
    fun `verify iterator next returns valid data with default values`() {
        val userIterator = excelDB.getIterator<UserWithDefault>()
        USER_WITH_DEFAULTS.forEach {
            assertTrue { userIterator.hasNext() }
            assertEquals(it, userIterator.next())
        }
        assertFalse(userIterator.hasNext())
    }

    @Test
    fun `verify to read bool and formula types cells`() {
        val boolAndFormulaIterator = excelDB.getIterator<BoolAndFormula>()
        BOOL_AND_FORMULAS.forEach {
            assertEquals(it, boolAndFormulaIterator.next())
        }
    }

    @Test
    fun `verify to normalize the column name when it contains special character`() {
        val specialCharactersIterator = excelDB.getIterator<SpecialCharacters>()
        SPECIAL_CHARACTERS.forEach {
            assertEquals(it, specialCharactersIterator.next())
        }
    }

    @Test
    fun `verify iterator doesn't throw an exception when the column is annotated`() {
        assertDoesNotThrow {
            excelDB.getIterator<UserWithAnnotations>()
        }
    }

}
