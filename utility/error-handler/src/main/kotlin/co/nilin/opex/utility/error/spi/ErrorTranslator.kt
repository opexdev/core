package co.nilin.opex.utility.error.spi

import co.nilin.opex.utility.error.data.OpexException

interface ErrorTranslator {

    fun translate(ex: OpexException): ExceptionResponse

}