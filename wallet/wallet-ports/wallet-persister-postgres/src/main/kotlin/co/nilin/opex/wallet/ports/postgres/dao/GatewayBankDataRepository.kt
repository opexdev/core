package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.BankDataModel
import co.nilin.opex.wallet.ports.postgres.model.GatewayBankDataModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface GatewayBankDataRepository : ReactiveCrudRepository<GatewayBankDataModel, Long> {

    fun deleteByBankDataIdAndGatewayId(bankDataId: Long, gatewayId: Long): Mono<Void>

    @Query("select b.* from gateway_bank_data gb join bank_data b on gb.bank_data_id=b.id where gb.gateway_id=:gatewayId ")
    fun findByGatewayId(gatewayId: Long): Flux<BankDataModel>?
}