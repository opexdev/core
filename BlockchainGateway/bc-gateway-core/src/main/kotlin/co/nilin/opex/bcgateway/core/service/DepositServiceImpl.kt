package co.nilin.opex.bcgateway.core.service

import co.nilin.opex.bcgateway.core.api.DepositService
import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.spi.DepositHandler
import org.springframework.stereotype.Service

@Service
class DepositServiceImpl(private val depositHandler: DepositHandler) : DepositService {

    override suspend fun getDepositDetails(refs: List<String>): List<Deposit> {
        return depositHandler.findDepositsByHash(refs)
    }
}