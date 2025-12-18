import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import model.TaxResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.math.BigDecimal

class MainIntegrationTest {

    private val mapper = jacksonObjectMapper()

    private lateinit var originalIn: java.io.InputStream
    private lateinit var originalOut: PrintStream

    @BeforeEach
    fun setUp() {
        originalIn = System.`in`
        originalOut = System.out
    }

    @AfterEach
    fun tearDown() {
        System.setIn(originalIn)
        System.setOut(originalOut)
    }

    private fun runCliLines(vararg jsonLines: String): List<List<TaxResult>> {
        // A CLI lê "1 simulação por linha" e para ao encontrar linha em branco.
        val input = buildString {
            jsonLines.forEach { append(it.trim()).append('\n') }
            append('\n') // linha vazia -> fim
        }

        System.setIn(ByteArrayInputStream(input.toByteArray()))

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        main()

        val output = outputStream.toString().trim()
        if (output.isBlank()) return emptyList()

        return output.lines()
            .filter { it.isNotBlank() }
            .map { line -> mapper.readValue<List<TaxResult>>(line) }
    }

    private fun assertTaxes(actual: List<TaxResult>, expected: List<String>) {
        assertEquals(expected.size, actual.size)
        for (i in expected.indices) {
            // compareTo ignora scale (0 vs 0.0 vs 0.00) => teste não fica frágil por formatação
            assertEquals(0, BigDecimal(expected[i]).compareTo(actual[i].tax))
        }
    }

    @Test
    fun `case 1`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":100},{"operation":"sell","unit-cost":15.00,"quantity":50},{"operation":"sell","unit-cost":15.00,"quantity":50}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "0.0", "0.0"))
    }

    @Test
    fun `case 2`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"sell","unit-cost":20.00,"quantity":5000},{"operation":"sell","unit-cost":5.00,"quantity":5000}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "10000.0", "0.0"))
    }

    @Test
    fun `case 1 + case 2 em duas linhas sao simulacoes independentes`() {
        val line1 = """[{"operation":"buy","unit-cost":10.00,"quantity":100},{"operation":"sell","unit-cost":15.00,"quantity":50},{"operation":"sell","unit-cost":15.00,"quantity":50}]"""
        val line2 = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"sell","unit-cost":20.00,"quantity":5000},{"operation":"sell","unit-cost":5.00,"quantity":5000}]"""

        val outputs = runCliLines(line1, line2)
        assertEquals(2, outputs.size)

        assertTaxes(outputs[0], listOf("0.0", "0.0", "0.0"))
        assertTaxes(outputs[1], listOf("0.0", "10000.0", "0.0"))
    }

    @Test
    fun `case 3`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"sell","unit-cost":5.00,"quantity":5000},{"operation":"sell","unit-cost":20.00,"quantity":3000}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "0.0", "1000.0"))
    }

    @Test
    fun `case 4`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"buy","unit-cost":25.00,"quantity":5000},{"operation":"sell","unit-cost":15.00,"quantity":10000}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "0.0", "0.0"))
    }

    @Test
    fun `case 5`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"buy","unit-cost":25.00,"quantity":5000},{"operation":"sell","unit-cost":15.00,"quantity":10000},{"operation":"sell","unit-cost":25.00,"quantity":5000}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "0.0", "0.0", "10000.0"))
    }

    @Test
    fun `case 6`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"sell","unit-cost":2.00,"quantity":5000},{"operation":"sell","unit-cost":20.00,"quantity":2000},{"operation":"sell","unit-cost":20.00,"quantity":2000},{"operation":"sell","unit-cost":25.00,"quantity":1000}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "0.0", "0.0", "0.0", "3000.0"))
    }

    @Test
    fun `case 7`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"sell","unit-cost":2.00,"quantity":5000},{"operation":"sell","unit-cost":20.00,"quantity":2000},{"operation":"sell","unit-cost":20.00,"quantity":2000},{"operation":"sell","unit-cost":25.00,"quantity":1000},{"operation":"buy","unit-cost":20.00,"quantity":10000},{"operation":"sell","unit-cost":15.00,"quantity":5000},{"operation":"sell","unit-cost":30.00,"quantity":4350},{"operation":"sell","unit-cost":30.00,"quantity":650}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "0.0", "0.0", "0.0", "3000.0", "0.0", "0.0", "3700.0", "0.0"))
    }

    @Test
    fun `case 8`() {
        val line = """[{"operation":"buy","unit-cost":10.00,"quantity":10000},{"operation":"sell","unit-cost":50.00,"quantity":10000},{"operation":"buy","unit-cost":20.00,"quantity":10000},{"operation":"sell","unit-cost":50.00,"quantity":10000}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "80000.0", "0.0", "60000.0"))
    }

    @Test
    fun `case 9`() {
        val line = """[{"operation":"buy","unit-cost":5000.00,"quantity":10},{"operation":"sell","unit-cost":4000.00,"quantity":5},{"operation":"buy","unit-cost":15000.00,"quantity":5},{"operation":"buy","unit-cost":4000.00,"quantity":2},{"operation":"buy","unit-cost":23000.00,"quantity":2},{"operation":"sell","unit-cost":20000.00,"quantity":1},{"operation":"sell","unit-cost":12000.00,"quantity":10},{"operation":"sell","unit-cost":15000.00,"quantity":3}]"""
        val outputs = runCliLines(line)
        assertEquals(1, outputs.size)
        assertTaxes(outputs[0], listOf("0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "1000.0", "2400.0"))
    }
}