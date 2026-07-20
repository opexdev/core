package co.nilin.opex.matching.engine.core.model

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

data class PreparedCommandResult(
    val pair: Pair,
    val commandId: String,
    val events: List<CoreEvent>,
    val stateTransition: PreparedStateTransition?
)