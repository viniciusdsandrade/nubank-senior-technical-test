package service

import model.Operation
import model.Portfolio
import java.math.BigDecimal
import java.math.BigDecimal.ZERO

class BuyHandler(
    private val policy: TaxPolicy
) : OperationHandler {

    override fun handle(operation: Operation, portfolio: Portfolio): BigDecimal {
        val currentQuantity = portfolio.quantity
        val buyQuantity = operation.quantity.toLong()
        val newQuantity = currentQuantity + buyQuantity

        val currentTotalCost = portfolio.avgPrice.multiply(BigDecimal.valueOf(currentQuantity))
        val buyTotalCost = operation.unitCost.multiply(BigDecimal.valueOf(buyQuantity))
        val newTotalCost = currentTotalCost.add(buyTotalCost)

        portfolio.quantity = newQuantity

        portfolio.avgPrice = if (newQuantity > 0L) {
            newTotalCost.divide(
                BigDecimal.valueOf(newQuantity),
                policy.avgPriceScale,
                policy.roundingMode
            )
        } else {
            ZERO
        }

        return ZERO
    }
}