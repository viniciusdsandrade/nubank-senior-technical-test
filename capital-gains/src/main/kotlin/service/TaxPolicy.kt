package service

import java.math.BigDecimal
import java.math.RoundingMode

data class TaxPolicy(
    val taxRate: BigDecimal = BigDecimal("0.20"),
    val exemptionThreshold: BigDecimal = BigDecimal("20000"),
    val moneyScale: Int = 2,
    val avgPriceScale: Int = 2,
    val roundingMode: RoundingMode = RoundingMode.HALF_EVEN
)