package extensions

import Entity
import FieldReference
import annotations.Sheet
import exceptions.NoKeyFieldException
import exceptions.PrimaryConstructorMissing
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/** Returns the primary constructor or throw a [PrimaryConstructorMissing] exception when it doesn't exist */
fun <T : Entity> KClass<T>.getPrimaryConstructor(): KFunction<T> =
    primaryConstructor ?: throw PrimaryConstructorMissing(simpleName)

/** Returns the field references */
fun <T : Entity> KClass<T>.getFieldReferences() = getPrimaryConstructor().parameters.map { FieldReference(this, it) }

/**
 * Returns the name of sheet by the [Sheet] annotation and the given parameter

 * @param T         An [Entity]
 * @param sheetName Name of sheet - when `null` the name will be calculated by the annotation or
 * the simple name of the class
 * */
fun <T : Entity> KClass<T>.getSheetName(sheetName: String?) =
    sheetName
        ?: findAnnotation<Sheet>()?.name?.takeIf { it.isNotBlank() }
        ?: simpleName.toString()

/** Returns the key field of a [KClass]<[Entity]> */
fun <T : Entity> KClass<T>.getKeyField(): KParameter {
    return with(getPrimaryConstructor().parameters) {
        filter { it.isKeyColumn }.getKeyField()
            ?: filter { it.isIdColumn }.getKeyField()
            ?: throw NoKeyFieldException()
    }
}
