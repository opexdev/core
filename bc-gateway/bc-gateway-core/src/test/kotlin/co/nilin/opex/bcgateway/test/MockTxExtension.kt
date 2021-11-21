package co.nilin.opex.bcgateway.test

import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.reactive.TransactionCallback
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


val OPERATOR = object : TransactionalOperator {
    override fun <T : Any?> transactional(mono: Mono<T>): Mono<T> {
        return Mono.empty()
    }

    override fun <T : Any?> execute(action: TransactionCallback<T>): Flux<T> {
        return Flux.from(action.doInTransaction(object : ReactiveTransaction {
            override fun isNewTransaction() = true

            override fun setRollbackOnly() {
            }

            override fun isRollbackOnly() = false

            override fun isCompleted() = true
        }))
    }
}
