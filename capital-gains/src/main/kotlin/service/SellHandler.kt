package service

import model.Operation
import model.Portfolio
import java.math.BigDecimal
import java.math.BigDecimal.ZERO


class SellHandler(
    private val policy: TaxPolicy
) : OperationHandler {

    override fun handle(operation: Operation, portfolio: Portfolio): BigDecimal {
        val quantity = operation.quantity.toLong()
        val quantityBD = BigDecimal.valueOf(quantity)

        val unitPrice = operation.unitCost
        val saleValue = unitPrice.multiply(quantityBD)

        val avgPrice = portfolio.avgPrice
        val grossProfit = unitPrice.subtract(avgPrice).multiply(quantityBD)

        val previousAccumulatedLoss = portfolio.accumulatedLoss

        portfolio.quantity -= quantity
        if (portfolio.quantity == 0L) {
            portfolio.avgPrice = ZERO
        }

        if (grossProfit <= ZERO) {
            portfolio.accumulatedLoss = previousAccumulatedLoss.add(grossProfit.abs())
            return ZERO
        }

        if (saleValue <= policy.exemptionThreshold) {
            return ZERO
        }

        val taxableProfit = grossProfit.subtract(previousAccumulatedLoss)
        if (taxableProfit <= ZERO) {
            portfolio.accumulatedLoss = taxableProfit.abs()
            return ZERO
        }

        portfolio.accumulatedLoss = ZERO
        return taxableProfit
            .multiply(policy.taxRate)
            .setScale(policy.moneyScale, policy.roundingMode)
    }
}
