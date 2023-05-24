package exceptions

import mu.KotlinLogging

/**
 * This common exception is the base of the other ExcelDB exceptions
 *
 * @param message            the detail message.
 * @param cause              the cause. (A `null` value is permitted,
 *                                       and indicates that the cause is nonexistent or unknown.)
 * @param enableSuppression  whether suppression is enabled or disabled
 * @param writableStackTrace whether the stack trace should be writable
 */
open class GeneralExcelException(
    message: String? = null,
    cause: Throwable? = null,
    enableSuppression: Boolean = false,
    writableStackTrace: Boolean = true
) : RuntimeException(message, cause, enableSuppression, writableStackTrace) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    init {
        logger.error { message }
    }
}
