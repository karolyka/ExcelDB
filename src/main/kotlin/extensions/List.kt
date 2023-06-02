package extensions

import ParameterMapper
import exceptions.MultipleKeyColumnException
import kotlin.reflect.KParameter

/** Returns the key field from a [List]<[KParameter]> if one and only one exist */
fun List<KParameter>.getKeyField(): KParameter? = getKeyField { it.fieldName }

/** Returns the key field from a [List]<[ParameterMapper]> if one and only one exist */
fun List<ParameterMapper>.getKeyField(): ParameterMapper? = getKeyField { it.kParameter.fieldName }

private fun <T> List<T>.getKeyField(mapper: (T) -> String): T? {
    if (size > 1) {
        throw MultipleKeyColumnException(joinToString { mapper(it) })
    }
    return if (size == 1) {
        first()
    } else {
        null
    }
}
