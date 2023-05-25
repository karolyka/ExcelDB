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
import extensions.iterate
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `verify iterator doesn't throw an exception with nested class`() {
        assertDoesNotThrow {
            excelDB.getIterator<Car>()
        }
    }

    @Test
    fun `verify iterator can read with nested class`() {
        assertDoesNotThrow {
            val carIterator = excelDB.getIterator<Car>()
            CARS.forEach {
                assertEquals(it, carIterator.next())
            }
        }
    }

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
    fun `verify iterator throw an exception with nested class when related data does not exists`() {
        assertFailsWith<KeyNotFoundException> {
            excelDB.getIterator<Car>("CarNotValidFactory").iterate()
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when related data does not exists`() {
        assertFailsWith<KeyNotFoundException> {
            excelDB.getData<Car>("CarNotValidFactory")
        }
    }

    @Test
    fun `verify getIterator throw an exception with nested class when there are multiple key columns`() {
        assertFailsWith<MultipleKeyColumnException> {
            excelDB.getIterator<CarWithMultipleKey>("Car")
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when there are multiple key columns`() {
        assertFailsWith<MultipleKeyColumnException> {
            excelDB.getData<CarWithMultipleKey>("Car")
        }
    }

    @Test
    fun `verify getIterator throw an exception with nested class when there is no key columns`() {
        assertFailsWith<NoKeyFieldException> {
            excelDB.getIterator<CarWithNoKeyField>("Car").iterate()
        }
    }

    @Test
    fun `verify iterator throw an exception with nested class when key-value does not exists`() {
        assertFailsWith<NullValueException> {
            excelDB.getIterator<Car>("CarNullKey").iterate()
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when key-value does not exists`() {
        assertFailsWith<NullValueException> {
            excelDB.getData<Car>("CarNullKey")
        }
    }

    @Test
    fun `verify iterator throw an exception with nested class when an unsupported key exists`() {
        assertFailsWith<UnsupportedCellTypeException> {
            excelDB.getIterator<CarWithUnsupportedKeyField>().iterate()
        }
    }

    @Test
    fun `verify getData throw an exception with nested class when an unsupported key exists`() {
        assertFailsWith<UnsupportedCellTypeException> {
            excelDB.getData<CarWithUnsupportedKeyField>()
        }
    }
}
