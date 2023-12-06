package co.nilin.opex.bcgateway.core.api

import co.nilin.opex.bcgateway.core.model.Deposit

interface DepositService {
    suspend fun getDepositDetails(refs: List<String>): List<Deposit>
}