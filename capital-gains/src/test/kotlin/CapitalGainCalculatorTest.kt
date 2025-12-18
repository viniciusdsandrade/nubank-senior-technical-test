import model.Operation
import model.OperationType.BUY
import model.OperationType.SELL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import service.CapitalGainCalculator
import java.math.BigDecimal
import java.util.stream.Stream
import org.junit.jupiter.params.provider.Arguments

class CapitalGainCalculatorTest {

    private val calc = CapitalGainCalculator()

    private fun assertMoneyEquals(expected: String, actual: BigDecimal) {
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }

    private fun assertTaxes(ops: List<Operation>, expectedTaxes: List<String>) {
        val taxes = calc.calculateTaxes(ops)
        assertEquals(expectedTaxes.size, taxes.size)
        for (i in expectedTaxes.indices) {
            assertMoneyEquals(expectedTaxes[i], taxes[i].tax)
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `cases oficiais`(name: String, ops: List<Operation>, expectedTaxes: List<String>) {
        assertTaxes(ops, expectedTaxes)
    }

    companion object {

        private fun operation(type: model.OperationType, unitCost: String, quantity: Int) =
            Operation(type, BigDecimal(unitCost), quantity)

        @JvmStatic
        fun cases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "Case #1",
                listOf(operation(BUY, "10.00", 100), operation(SELL, "15.00", 50), operation(SELL, "15.00", 50)),
                listOf("0.0", "0.0", "0.0")
            ),
            Arguments.of(
                "Case #2",
                listOf(operation(BUY, "10.00", 10000), operation(SELL, "20.00", 5000), operation(SELL, "5.00", 5000)),
                listOf("0.0", "10000.0", "0.0")
            ),
            Arguments.of(
                "Case #3",
                listOf(operation(BUY, "10.00", 10000), operation(SELL, "5.00", 5000), operation(SELL, "20.00", 3000)),
                listOf("0.0", "0.0", "1000.0")
            ),
            Arguments.of(
                "Case #4",
                listOf(operation(BUY, "10.00", 10000), operation(BUY, "25.00", 5000), operation(SELL, "15.00", 10000)),
                listOf("0.0", "0.0", "0.0")
            ),
            Arguments.of(
                "Case #5",
                listOf(operation(BUY, "10.00", 10000), operation(BUY, "25.00", 5000), operation(SELL, "15.00", 10000), operation(SELL, "25.00", 5000)),
                listOf("0.0", "0.0", "0.0", "10000.0")
            ),
            Arguments.of(
                "Case #6",
                listOf(operation(BUY, "10.00", 10000), operation(SELL, "2.00", 5000), operation(SELL, "20.00", 2000), operation(SELL, "20.00", 2000), operation(SELL, "25.00", 1000)),
                listOf("0.0", "0.0", "0.0", "0.0", "3000.0")
            ),
            Arguments.of(
                "Case #7",
                listOf(
                    operation(BUY, "10.00", 10000), operation(SELL, "2.00", 5000), operation(SELL, "20.00", 2000), operation(SELL, "20.00", 2000), operation(SELL, "25.00", 1000),
                    operation(BUY, "20.00", 10000), operation(SELL, "15.00", 5000), operation(SELL, "30.00", 4350), operation(SELL, "30.00", 650)
                ),
                listOf("0.0", "0.0", "0.0", "0.0", "3000.0", "0.0", "0.0", "3700.0", "0.0")
            ),
            Arguments.of(
                "Case #8",
                listOf(operation(BUY, "10.00", 10000), operation(SELL, "50.00", 10000), operation(BUY, "20.00", 10000), operation(SELL, "50.00", 10000)),
                listOf("0.0", "80000.0", "0.0", "60000.0")
            ),
            Arguments.of(
                "Case #9",
                listOf(
                    operation(BUY, "5000.00", 10), operation(SELL, "4000.00", 5), operation(BUY, "15000.00", 5), operation(BUY, "4000.00", 2),
                    operation(BUY, "23000.00", 2), operation(SELL, "20000.00", 1), operation(SELL, "12000.00", 10), operation(SELL, "15000.00", 3)
                ),
                listOf("0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "1000.0", "2400.0")
            )
        )
    }
}
