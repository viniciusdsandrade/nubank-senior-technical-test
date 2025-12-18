package service

import model.Operation
import model.Portfolio
import java.math.BigDecimal

interface OperationHandler {
    fun handle(operation: Operation, portfolio: Portfolio): BigDecimal
}