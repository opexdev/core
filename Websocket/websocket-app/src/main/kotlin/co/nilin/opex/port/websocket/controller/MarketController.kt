package co.nilin.opex.port.websocket.controller

import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class MarketController {

    @MessageMapping("/market/depth")
    fun orderBook(@Header("symbol") symbol:String):List<Any>{
        return emptyList()
    }

}