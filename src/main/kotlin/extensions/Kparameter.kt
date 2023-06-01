package extensions

import Entity
import annotations.Column
import exceptions.UnsupportedParameterException
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

/**
 * It is provided the name of parameter by [Column] annotation or by name if the annotation doesn't exist
 *
 * It throws an [UnsupportedParameterException] when name is `null`
 * */
val KParameter.fieldName: String
    get() = columnName ?: throw UnsupportedParameterException()

/** `false` if the parameter is optional and can be omitted when making a call via [KCallable.callBy],
 * or `true` otherwise. */
val KParameter.isRequired: Boolean
    get() = !isOptional

/** It is provided the column name of parameter by [Column] annotation or by name if the annotation doesn't exist */
val KAnnotatedElement.columnName: String?
    get() = findAnnotation<Column>()?.name?.takeIf { it.isNotBlank() }
        ?: (this as? KParameter)?.name

/** It is provided the [KParameter] as [KClass]<[Entity]> or `null` if it's not an [Entity] */
val KParameter.asEntity: KClass<Entity>?
    get() = (type.classifier as KClass<*>).let { kClass ->
        if (kClass.isSubclassOf(Entity::class)) {
            @Suppress("UNCHECKED_CAST")
            kClass as KClass<Entity>
        } else {
            null
        }
    }

/** Returns `true` when the [KAnnotatedElement] has the [Column] annotation
 * and the [Column.keyColumn] field is also `true` */
val KAnnotatedElement.isKeyColumn: Boolean
    get() = findAnnotation<Column>()?.keyColumn == true

/** Returns `true` when the field name of [KParameter] is "id" */
val KParameter.isIdColumn: Boolean
    get() = fieldName == "id"
