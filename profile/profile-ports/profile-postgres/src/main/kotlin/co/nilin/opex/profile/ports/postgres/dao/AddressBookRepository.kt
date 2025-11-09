package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.profile.AddressBook
import co.nilin.opex.profile.ports.postgres.model.entity.AddressBookModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface AddressBookRepository : ReactiveCrudRepository<AddressBookModel, Long> {

    @Query("select * from address_book where uuid = :uuid")
    suspend fun findAllByUuid(uuid: String): Flux<AddressBook>

}