package co.nilin.opex.matching.core.eventh

import co.nilin.opex.matching.core.eventh.events.CoreEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.suspendCoroutine

object EventDispatcher {

    private val eventsHandler = mutableMapOf<Class<*>, MutableList<EventListener<*>>>()

    @JvmStatic
    inline fun <reified T> register(noinline lambda: (T) -> Unit) = register(T::class.java, lambda)

    @JvmStatic
    fun <T> register(type: Class<T>, lambda: (T) -> Unit) = register(type, EventListener(lambda))

    @JvmStatic
    fun <T> register(type: Class<T>, listener: EventListener<T>) {
        eventsHandler.getOrPut(type, { LinkedList() }).add(listener)
    }


    fun emit(event: CoreEvent) {
        var type: Class<*>? = event::class.java
        while (type != null) {
            eventsHandler[type]?.forEach { eventsHandler ->
                kotlin.runCatching {
                    eventsHandler(event)
                }
            }
            type = type.superclass
        }
    }


    open class EventListener<T>(
        val lambda: (T) -> Unit
    ) {
        operator fun invoke(event: Any) {
            lambda(event as T)
        }
    }
}