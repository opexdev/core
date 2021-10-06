package co.nilin.opex.port.bcgateway.walletproxy.impl

import co.nilin.opex.bcgateway.core.spi.WalletProxy
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import java.math.BigDecimal

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class WalletProxyImpl : WalletProxy {
    override fun transfer(uuid: String, symbol: String, amount: BigDecimal) {
        TODO("Not yet implemented")
    }
}
