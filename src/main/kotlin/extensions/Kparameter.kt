package extensions

import annotations.Column
import exceptions.UnsupportedParameterException
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

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
