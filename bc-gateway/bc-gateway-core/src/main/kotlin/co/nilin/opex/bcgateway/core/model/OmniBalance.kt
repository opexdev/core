package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class OmniBalance(val currency: String, val network: String, val balance: BigDecimal? = BigDecimal.ZERO)

