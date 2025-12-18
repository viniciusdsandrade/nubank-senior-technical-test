import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import model.Operation
import model.TaxResult
import service.CapitalGainCalculator

fun main() {
    val mapper = jacksonObjectMapper()
    val calculator = CapitalGainCalculator()

    val reader = System.`in`.bufferedReader()
    while (true) {
        val line = reader.readLine() ?: break
        if (line.isBlank()) continue

        val operations: List<Operation> = mapper.readValue(line)
        val taxes: List<TaxResult> = calculator.calculateTaxes(operations)
        println(mapper.writeValueAsString(taxes))
    }
}