package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.spi.AddressManager
import org.springframework.stereotype.Component

@Component
class AddressManagerImpl :AddressManager{
    override fun revokeExpiredAddress() {

    }



}