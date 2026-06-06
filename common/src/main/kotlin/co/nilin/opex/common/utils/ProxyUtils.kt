package co.nilin.opex.common.utils

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T : Any?> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}
