package co.nilin.opex.bcgateway.ports.postgres.dao

import co.nilin.opex.bcgateway.ports.postgres.model.ReservedAddressModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ReservedAddressRepository : ReactiveCrudRepository<ReservedAddressModel, Long> {

    @Query("select * from reserved_addresses where address = :address and (memo is null or memo = '' or memo = :memo) limit 1")
    fun findByAddressAndMemo(address: String, memo: String?): Mono<ReservedAddressModel>

    /**
     * Takes one address out of the pool in a single statement and returns it. The row lock plus
     * `SKIP LOCKED` makes concurrent callers land on different rows instead of racing for the same
     * one, and because the DELETE is what marks an address as taken, two users can never end up
     * holding it. Returns empty when the pool has nothing left for this type.
     *
     * Ordered by id rather than at random so it stays an index scan: this runs on the deposit
     * path too, and the caller only needs "an address I did not choose", not an unpredictable one.
     */
    @Query(
        """
    DELETE FROM reserved_addresses
    WHERE id = (SELECT id FROM reserved_addresses
                WHERE address_type = :addressType
                ORDER BY id
                LIMIT 1 FOR UPDATE SKIP LOCKED)
    RETURNING *
"""
    )
    fun claimByType(@Param("addressType") addressType: Long): Mono<ReservedAddressModel>

    /**
     * Same atomic claim as [claimByType] without narrowing the type, for the admin flow where the
     * pool decides which address to hand out and therefore also decides its type.
     */
    @Query(
        """
    DELETE FROM reserved_addresses
    WHERE id = (SELECT id FROM reserved_addresses
                ORDER BY id
                LIMIT 1 FOR UPDATE SKIP LOCKED)
    RETURNING *
"""
    )
    fun claimAny(): Mono<ReservedAddressModel>

    /**
     * Same atomic claim as [claimByType] for an address the admin picked from the pool listing.
     * Returns empty when it is already gone, which is how a double assignment is detected.
     */
    @Query(
        """
    DELETE FROM reserved_addresses
    WHERE id = (SELECT id FROM reserved_addresses
                WHERE address = :address AND coalesce(memo, '') = :memo
                LIMIT 1 FOR UPDATE SKIP LOCKED)
    RETURNING *
"""
    )
    fun claimByAddress(@Param("address") address: String, @Param("memo") memo: String): Mono<ReservedAddressModel>

    @Query(
        """
    SELECT * FROM reserved_addresses r
    WHERE (:addressTypeId IS NULL OR r.address_type = :addressTypeId)
      AND (:chain IS NULL OR r.address_type IN
           (SELECT addr_type_id FROM chain_address_types WHERE chain_name = :chain))
      AND (:address IS NULL OR r.address = :address)
    ORDER BY r.id
    LIMIT :limit OFFSET :offset
"""
    )
    fun searchReserved(
        @Param("addressTypeId") addressTypeId: Long?,
        @Param("chain") chain: String?,
        @Param("address") address: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flow<ReservedAddressModel>

    @Query(
        """
    SELECT count(*) FROM reserved_addresses r
    WHERE (:addressTypeId IS NULL OR r.address_type = :addressTypeId)
      AND (:chain IS NULL OR r.address_type IN
           (SELECT addr_type_id FROM chain_address_types WHERE chain_name = :chain))
      AND (:address IS NULL OR r.address = :address)
"""
    )
    fun countReserved(
        @Param("addressTypeId") addressTypeId: Long?,
        @Param("chain") chain: String?,
        @Param("address") address: String?
    ): Mono<Long>
}
