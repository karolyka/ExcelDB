import domain.car.Car
import domain.car.CarFactory
import domain.car.CarWithEngine
import domain.car.CarWithMultipleKey
import domain.car.CarWithNoKeyField
import domain.car.CarWithUnsupportedKeyField
import exceptions.KeyNotFoundException
import exceptions.MultipleKeyColumnException
import exceptions.NoKeyFieldException
import exceptions.NullValueException
import exceptions.UnsupportedCellTypeException
import extensions.getData
import extensions.writeDataToWorkbook
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val SHEET_NAME = "CarSheet"

class ExcelDBNestedTest {
    companion object {
        private val TOYOTA = CarFactory("Toyota", "Toyota Car")
        private val FERRARI = CarFactory("Ferrari", "Ferrari Car")
        private val HONDA = CarFactory("Honda", "Honda Car")
        private val CARS = listOf(
            Car(1, "Prius", TOYOTA),
            Car(2, "F40", FERRARI),
            Car(3, "Yaris", TOYOTA),
            Car(4, "Civic", HONDA)
        )
        private val CARS_WITH_ENGINE = listOf(
            CarWithEngine(1, "Prius", TOYOTA, TOYOTA),
            CarWithEngine(2, "F40", FERRARI, FERRARI),
            CarWithEngine(3, "Yaris", TOYOTA, null),
            CarWithEngine(4, "Civic", HONDA, null)
        )
    }

    private val excelDB: ExcelDB = ExcelDB(TEST_WORKBOOK)

    @Test
    fun `verify getData can read with nested class`() {
        assertDoesNotThrow {
            assertEquals(CARS, excelDB.getData<Car>())
        }
    }

    @Test
    fun `verify getData can read with nested class and optional field`() {
        assertDoesNotThrow {
            assertEquals(CARS_WITH_ENGINE, excelDB.getData<CarWithEngine>())
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when related data does not exists`() {
        assertFailsWith<KeyNotFoundException> {
            excelDB.getData<Car>("CarNotValidFactory")
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when there are multiple key columns`() {
        assertFailsWith<MultipleKeyColumnException> {
            excelDB.getData<CarWithMultipleKey>("Car")
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when there is no key columns`() {
        assertFailsWith<NoKeyFieldException> {
            excelDB.getData<CarWithNoKeyField>("Car")
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when key-value does not exists`() {
        assertFailsWith<NullValueException> {
            excelDB.getData<Car>("CarNullKey")
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when an unsupported key exists`() {
        assertFailsWith<UnsupportedCellTypeException> {
            excelDB.getData<CarWithUnsupportedKeyField>()
        }
    }

    @Test
    fun `verify writeData saves all records with nested data`() {
        val excelDB = ExcelDB(TEST_WORKBOOK_FOR_WRITE, FileMode.CREATE)
        excelDB.writeDataToWorkbook(CARS)
        excelDB.writeWorkbook()

        assertTrue { File(TEST_WORKBOOK_FOR_WRITE).exists() }
        assertEquals(CARS, ExcelDB(TEST_WORKBOOK_FOR_WRITE).getData<Car>())
    }

    @Test
    fun `verify writeData saves all records with nested data with different sheet name`() {
        val excelDB = ExcelDB(TEST_WORKBOOK_FOR_WRITE, FileMode.CREATE)
        excelDB.writeDataToWorkbook(CARS, SHEET_NAME)
        excelDB.writeWorkbook()

        assertTrue { File(TEST_WORKBOOK_FOR_WRITE).exists() }
        assertEquals(CARS, ExcelDB(TEST_WORKBOOK_FOR_WRITE).getData<Car>(SHEET_NAME))
    }
}
