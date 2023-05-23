import domain.BoolAndFormula
import domain.SpecialCharacters
import domain.User
import domain.UserWithDefault
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date

const val EMPTY_USER = "EmptyUser"
const val MISSING = "Missing"
const val TEST_WORKBOOK = "src/test/resources/data/TestWorkBook.xlsx"

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

fun String.toDate(): Date = SimpleDateFormat("yyyy-MM-dd").parse(this)
fun String.toDateTime(): Date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(this)
