package co.nilin.opex.admin.core.spi

import co.nilin.opex.admin.core.events.AdminEvent

interface AdminEventPublisher {

    suspend fun publish(event: AdminEvent)

}