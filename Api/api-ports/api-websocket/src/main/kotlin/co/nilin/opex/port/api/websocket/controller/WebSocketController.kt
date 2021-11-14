package co.nilin.opex.port.api.websocket.controller

import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller

@Controller
class WebSocketController {

    @SubscribeMapping("/test")
    fun test(): String {
        print("")
        return ""
    }

}