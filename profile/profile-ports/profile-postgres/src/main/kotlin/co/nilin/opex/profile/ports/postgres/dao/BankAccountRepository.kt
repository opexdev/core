package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.profile.BankAccount
import co.nilin.opex.profile.ports.postgres.model.entity.BankAccountModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface BankAccountRepository : ReactiveCrudRepository<BankAccountModel, Long> {

    @Query("select * from bank_account where uuid = :uuid")
    suspend fun findAllByUuid(uuid: String): Flux<BankAccount>

    @Query(
        """
    SELECT * 
    FROM bank_account 
    WHERE 
        (:cardNumber IS NOT NULL AND card_number = :cardNumber)
        OR
        (:iban IS NOT NULL AND iban = :iban)
"""
    )
    suspend fun findByCardNumberOrIban(
        cardNumber: String?,
        iban: String?
    ): Flux<BankAccount>

    @Query(
        """
    SELECT * 
    FROM bank_account 
    WHERE 
        ((:cardNumber IS NOT NULL AND card_number = :cardNumber)
        OR
        (:iban IS NOT NULL AND iban = :iban))
        AND uuid = :uuid
"""
    )
    suspend fun findByUuidAndCardNumberOrIban(
        uuid: String,
        cardNumber: String?,
        iban: String?
    ): Flux<BankAccount>

}