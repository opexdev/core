package co.nilin.opex.api.ports.proxy.utils

import org.springframework.http.*

//fun <T> tryRest(action: () -> T): T {
//    return try {
//        action()
//    }
//    catch (e: HttpClientErrorException) {
//        logger.error("Client error fetching pair configs from $url: ${e.statusCode} - ${e.responseBodyAsString}")
//        throw e
//    } catch (e: HttpServerErrorException) {
//        logger.error("Server error fetching pair configs from $url: ${e.statusCode} - ${e.responseBodyAsString}")
//        throw e
//    } catch (e: ResourceAccessException) {
//        logger.error("Network or connection error fetching pair configs from $url: ${e.message}")
//        throw e
//    } catch (e: Exception) {
//        logger.error("Unexpected error fetching pair configs from $url: ${e.message}", e)
//        throw e
//    }
//}

internal fun defaultHeaders() = HttpHeaders().apply {
    add(HttpHeaders.ACCEPT, "application/json")
    add(HttpHeaders.CONTENT_TYPE, "application/json")
}

internal fun defaultHeaders(contentType: MediaType) = HttpHeaders().apply {
    add(HttpHeaders.ACCEPT, "application/json")
    this.contentType = contentType
}

internal fun body(body: Any) = HttpEntity(body, defaultHeaders())

internal fun body(body: Any, auth: String?) = HttpEntity(body, defaultHeaders().withAuth(auth))

internal fun noBody() = HttpEntity<String>(defaultHeaders())

internal fun noBody(auth: String?) = HttpEntity<String>(defaultHeaders().withAuth(auth))

internal fun HttpHeaders.withAuth(auth: String?): HttpHeaders {
    add("Authorization", "Bearer $auth")
    return this
}