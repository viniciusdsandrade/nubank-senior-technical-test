package model


import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class Operation(
    val operation: OperationType,
    @JsonProperty("unit-cost")
    val unitCost: BigDecimal,
    val quantity: Int
)