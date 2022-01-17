package co.nilin.opex.matching.engine.core.model

data class Bucket(val price: Long, var totalQuantity: Long, var ordersCount: Long, var lastOrder: SimpleOrder)