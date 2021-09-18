package co.nilin.opex.utility.error

import co.nilin.opex.utility.error.controller.ExceptionController
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(ExceptionController::class)
annotation class EnableOpexErrorHandler