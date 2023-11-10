import domain.BoolAndFormula
import domain.DataTypes
import domain.Missing
import domain.NoPrimaryConstructor
import domain.SpecialCharacters
import domain.Unsupported
import domain.user.User
import domain.user.UserMissingColumn
import domain.user.UserNonUniqueColumns
import domain.user.UserWithAnnotations
import domain.user.UserWithDefault
import domain.user.UserWithOptional
import domain.user.UserWithSheetAnnotation
import domain.user.UserWithUnsupportedField
import exceptions.ColumnNotFoundException
import exceptions.NonUniqueColumnException
import exceptions.PrimaryConstructorMissing
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
    fun `verify getData doesn't throw an exception when the column is annotated`() {
        assertDoesNotThrow {
            excelDB.getData<UserWithAnnotations>()
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
    fun `verify getData returns empty list when sheet has no data`() {
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

    @Test
    fun `verify getData throws exception when a parameter type is unsupported`() {
        assertFailsWith<UnsupportedDataTypeException> {
            excelDB.getData<Unsupported>()
        }
    }

    @Test
    fun `verify getData throws exception when there is no primary constructor`() {
        assertFailsWith<PrimaryConstructorMissing> {
            excelDB.getData<NoPrimaryConstructor>()
        }
    }

    @Test
    fun `verify getData can read all of the supported datatype`() {
        assertEquals(DATA_TYPES, excelDB.getData<DataTypes>())
    }
}
