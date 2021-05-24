package co.nilin.mixchange.port.accountant.postgres.dao

import co.nilin.mixchange.port.accountant.postgres.model.OrderModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface OrderRepository: ReactiveCrudRepository<OrderModel, Long> {
    @Query("select * from orders where ouid = :ouid")
    fun findByOuid(@Param("ouid") ouid: String): Mono<OrderModel>
}