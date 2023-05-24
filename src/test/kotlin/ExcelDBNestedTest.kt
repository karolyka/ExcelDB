import domain.car.Car
import domain.car.CarFactory
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class ExcelDBNestedTest {
    companion object {
        private val TOYOTA = CarFactory("Toyota", "Toyota Car")
        private val FERRARI = CarFactory("Ferrari", "Ferrari Car")
        private val CARS = listOf(
            Car(1, "Prius", TOYOTA),
            Car(2, "F40", FERRARI),
            Car(3, "Yaris", TOYOTA),
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
            val x = excelDB.getData<Car>()
            println(x)
        }
    }
}
