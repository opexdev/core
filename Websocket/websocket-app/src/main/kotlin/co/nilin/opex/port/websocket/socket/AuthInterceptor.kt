package co.nilin.opex.port.websocket.socket

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.stereotype.Component

@Component
class AuthInterceptor : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(ChannelInterceptor::class.java)

    @Autowired
    private lateinit var jwtDecoder: JwtDecoder
    private val converter = JwtAuthenticationConverter()

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
        if (accessor?.command == StompCommand.CONNECT) {
            val authorization = accessor.getNativeHeader("X-Authorization")
            logger.debug("Authorization: $authorization")

            if (authorization.isNullOrEmpty())
                return message

            val token = authorization[0]

            val jwt = jwtDecoder.decode(token)
            val auth = converter.convert(jwt)
            accessor.user = auth
        }
        return message
    }

}