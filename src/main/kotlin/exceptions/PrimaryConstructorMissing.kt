package exceptions

import kotlin.reflect.KClass

/**
 * This exception will be thrown when there is no primary constructor for a [KClass]
 *
 * @param kClassName Name of the class
 * */
class PrimaryConstructorMissing(kClassName: String?) :
    GeneralExcelException("Primary constructor is missing for $kClassName")
