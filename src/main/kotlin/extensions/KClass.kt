package extensions

import Entity
import exceptions.PrimaryConstructorMissing
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

/** Get the primary constructor or throw a [PrimaryConstructorMissing] exception when it doesn't exist */
fun <T : Entity> KClass<T>.getPrimaryConstructor(): KFunction<T> =
    primaryConstructor ?: throw PrimaryConstructorMissing(simpleName)
