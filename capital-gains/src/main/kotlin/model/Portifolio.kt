package model

import java.math.BigDecimal
import java.math.BigDecimal.ZERO

data class Portfolio(
    var quantity: Long = 0,
    var avgPrice: BigDecimal = ZERO,
    var accumulatedLoss: BigDecimal = ZERO
)