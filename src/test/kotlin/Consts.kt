import domain.BoolAndFormula
import domain.DataTypes
import domain.SpecialCharacters
import domain.user.User
import domain.user.UserWithDefault
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

const val EMPTY_USER = "EmptyUser"
const val MISSING = "Missing"

private const val TEST_WORKBOOK_BASE_PATH = "src/test/resources/data/"
const val TEST_WORKBOOK = "${TEST_WORKBOOK_BASE_PATH}TestWorkBook.xlsx"
const val TEST_WORKBOOK_FOR_WRITE = "${TEST_WORKBOOK_BASE_PATH}TestWorkBookW.xlsx"

val USERS = listOf(
    User(1, "One"),
    User(2, "Two"),
    User(3, "Three")
)

val USER_WITH_DEFAULTS = listOf(
    UserWithDefault(1, "One", "one@one.one"),
    UserWithDefault(2, "Two"),
    UserWithDefault(3, "Three")
)

val BOOL_AND_FORMULAS = listOf(
    BoolAndFormula(false, 2, "A", LocalDateTime.of(2023, 4, 27, 0, 0), "2023-04-28".toDate()),
    BoolAndFormula(true, 6, "AB", LocalDateTime.of(2023, 5, 1, 13, 4, 15), "2023-05-02 14:05:16".toDateTime()),
    BoolAndFormula(true, 8, "ab", LocalDateTime.of(2023, 5, 2, 13, 4, 15), "2023-07-02 00:00:00".toDateTime())
)

val SPECIAL_CHARACTERS = listOf(
    SpecialCharacters(1, "col1", "col2", "col3")
)

val DATA_TYPES = listOf(
    DataTypes(
        id = 1,
        boolean = false,
        calendar = Calendar.Builder().setInstant("2023-12-31 23:59:59".toDateTime()).build(),
        date = "2023-12-31".toDate(),
        double = 25.0,
        int = 12,
        localDate = LocalDate.of(2023, 6, 2),
        localDateTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59),
        localTime = LocalTime.of(0, 0)
    ),
    DataTypes(
        id = 2,
        boolean = true,
        calendar = Calendar.Builder().setInstant("2023-12-31 23:59:59".toDateTime()).build(),
        date = "2099-12-31".toDate(),
        double = 25.2,
        int = 123456,
        localDate = LocalDate.of(2099, 12, 31),
        localDateTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59),
        localTime = LocalTime.of(23, 59, 59)
    )
)

private fun String.toDate(): Date = SimpleDateFormat("yyyy-MM-dd").parse(this)
private fun String.toDateTime(): Date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(this)
