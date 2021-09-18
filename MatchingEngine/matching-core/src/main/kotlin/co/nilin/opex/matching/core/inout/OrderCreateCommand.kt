package co.nilin.opex.matching.core.inout

import co.nilin.opex.matching.core.model.MatchConstraint
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.matching.core.model.OrderType
import co.nilin.opex.matching.core.model.Pair


data class OrderCreateCommand(val ouid: String,
                              val uuid: String,
                              val pair: Pair,
                              val price: Long,
                              val quantity: Long,
                              val direction: OrderDirection,
                              val matchConstraint: MatchConstraint,
                              val orderType: OrderType)