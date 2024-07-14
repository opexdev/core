package co.nilin.opex.bcgateway.omniwallet.model

import java.math.BigDecimal

data class AddressBalanceWithUsd(val address: String, val balance: BigDecimal, val balanceUsd: BigDecimal)


data class ChainBalanceResponse(val data:List<AddressBalanceWithUsd>)