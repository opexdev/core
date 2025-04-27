package co.nilin.opex.wallet.app.config

import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService(
    @Value("\${app.system.uuid}")
    private val systemUuid: String,
    @Value("b58dc8b2-9c0f-11ee-8c90-0242ac120002")
    private val adminUuid: String,
    private val walletOwnerRepository: WalletOwnerRepository
) {

    @PostConstruct
    fun init() = runBlocking {
        // addUserLimits()
        addSystemAndAdminWallet()
    }

    private suspend fun addSystemAndAdminWallet() = coroutineScope {
        if (!walletOwnerRepository.existsById(1).awaitSingle()) {
            walletOwnerRepository.save(WalletOwnerModel(null, systemUuid, "system", "system"))
                .awaitSingleOrNull()
        }

        walletOwnerRepository.findByUuid(adminUuid).awaitSingleOrNull()
            ?: walletOwnerRepository.save(
                WalletOwnerModel(
                    null,
                    adminUuid,
                    "admin",
                    "admin"
                )
            ).awaitSingleOrNull()
    }
}
