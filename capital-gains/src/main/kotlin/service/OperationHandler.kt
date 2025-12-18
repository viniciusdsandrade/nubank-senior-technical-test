package service

import model.Operation
import model.Portfolio
import java.math.BigDecimal

interface OperationHandler {
    fun handle(op: Operation, portfolio: Portfolio): BigDecimal
}