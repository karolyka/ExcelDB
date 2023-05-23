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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExcelDBGetDataTest {

    private val excelDB: ExcelDB = ExcelDB(TEST_WORKBOOK)

    @Test
    fun `verify getData throws exception when the sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getData<Missing>()
        }
    }

    @Test
    fun `verify getData throws exception when the provided sheet doesn't exists`() {
        assertFailsWith<SheetNotFoundException> {
            excelDB.getData<User>(MISSING)
        }
    }

    @Test
    fun `verify getData throws exception when a required column is missing`() {
        assertFailsWith<ColumnNotFoundException> {
            excelDB.getData<UserMissingColumn>()
        }
    }

    @Test
    fun `verify getData throws exception when a column mapping is not unique`() {
        assertFailsWith<NonUniqueColumnException> {
            excelDB.getData<UserNonUniqueColumns>()
        }
    }

    @Test
    fun `verify getData doesn't throws exception when an optional column is missing`() {
        assertDoesNotThrow {
            excelDB.getData<UserWithOptional>()
        }
    }

    @Test
    fun `verify getData doesn't throws exception when an sheet annotation has valid name`() {
        assertDoesNotThrow {
            excelDB.getData<UserWithSheetAnnotation>()
        }
    }

    @Test
    fun `verify getData returns all records when sheet has data`() {
        assertEquals(USERS, excelDB.getData<User>())
    }

    @Test
    fun `verify getData returns all records when an sheet annotation has valid name with class parameter`() {
        assertEquals(USERS, excelDB.getData(User::class))
    }

    @Test
    fun `verify getData returns empty list when sheet has no data with class parameter`() {
        assertEquals(emptyList(), excelDB.getData<User>(EMPTY_USER))
    }

    @Test
    fun `verify getData hasNext returns false when sheet has no data`() {
        assertEquals(emptyList(), excelDB.getData(User::class, EMPTY_USER))
    }

    @Test
    fun `verify getData next throws exception when a field type is unsupported`() {
        assertFailsWith<UnsupportedDataTypeException> {
            excelDB.getData<UserWithUnsupportedField>()
        }
    }

    @Test
    fun `verify getData next returns valid data with default values`() {
        assertEquals(USER_WITH_DEFAULTS, excelDB.getData<UserWithDefault>())
    }

    @Test
    fun `verify to read bool and formula types cells`() {
        assertEquals(BOOL_AND_FORMULAS, excelDB.getData<BoolAndFormula>())
    }

    @Test
    fun `verify to normalize the column name when it contains special character`() {
        assertEquals(SPECIAL_CHARACTERS, excelDB.getData<SpecialCharacters>())
    }
}