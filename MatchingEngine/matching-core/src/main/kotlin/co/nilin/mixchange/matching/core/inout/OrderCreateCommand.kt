package co.nilin.mixchange.matching.core.inout

import co.nilin.mixchange.matching.core.model.MatchConstraint
import co.nilin.mixchange.matching.core.model.OrderDirection
import co.nilin.mixchange.matching.core.model.OrderType
import co.nilin.mixchange.matching.core.model.Pair


data class OrderCreateCommand(val ouid: String,
                              val uuid: String,
                              val pair: Pair,
                              val price: Long,
                              val quantity: Long,
                              val direction: OrderDirection,
                              val matchConstraint: MatchConstraint,
                              val orderType: OrderType)