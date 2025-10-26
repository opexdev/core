package co.nilin.opex.device.core.spi

import co.nilin.opex.device.core.data.LoginEvent


interface LoginEventListener {
        fun id(): String
        fun onEvent(event: LoginEvent, partition: Int, offset: Long, timestamp: Long, eventId: String)

    }
