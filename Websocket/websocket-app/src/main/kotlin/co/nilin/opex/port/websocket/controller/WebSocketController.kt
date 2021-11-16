package co.nilin.opex.port.websocket.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class WebSocketController(private val template: SimpMessagingTemplate) {

    @SubscribeMapping("/test")
    fun test(): String {
        print("sss")
        return "sss"
    }

    @MessageMapping("/a")
    @SendTo("/secured/queue/a")
    fun a(): String {
        return "this is a"
    }

    @MessageMapping("/b")
    fun b(principal: Principal) {
        template.convertAndSendToUser(principal.name,"/secured/queue/b","this is b")
    }

}