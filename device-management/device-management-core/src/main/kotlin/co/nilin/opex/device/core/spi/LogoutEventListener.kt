package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.LogoutEvent


interface LogoutEventListener {
        fun id(): String
        fun onEvent(event: LogoutEvent, partition: Int, offset: Long, timestamp: Long, eventId: String)
    }
