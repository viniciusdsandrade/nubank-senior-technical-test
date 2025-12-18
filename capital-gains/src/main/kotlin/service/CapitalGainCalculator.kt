package service

import model.Operation
import model.OperationType.BUY
import model.OperationType.SELL
import model.Portfolio
import model.TaxResult

class CapitalGainCalculator(
    private val policy: TaxPolicy = TaxPolicy(),
    private val buyHandler: OperationHandler = BuyHandler(policy),
    private val sellHandler: OperationHandler = SellHandler(policy)
) {

    fun calculateTaxes(operations: List<Operation>): List<TaxResult> {
        val portfolio = Portfolio()
        val taxes = ArrayList<TaxResult>(operations.size)

        for (operation in operations) {
            val tax = when (operation.operation) {
                BUY -> buyHandler.handle(operation, portfolio)
                SELL -> sellHandler.handle(operation, portfolio)
            }
            taxes.add(TaxResult(tax))
        }

        return taxes
    }
}
