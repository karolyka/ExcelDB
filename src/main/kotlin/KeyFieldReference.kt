import extensions.getKeyField
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 *  This class holds the references of a key field
 *
 *  @param T      An [Entity]
 *  @param kClass [KClass] of the [Entity]
 *  */
class KeyFieldReference<T : Entity>(kClass: KClass<T>) {
    private val keyField: KParameter by lazy { kClass.getKeyField() }
    private val fieldReference: FieldReference<T> by lazy { FieldReference(kClass, keyField) }

    /** The class of the key field */
    val keyFieldKClass: KClass<*> by lazy { keyField.type.classifier as KClass<*> }

    /** Returns the current value of the key field */
    fun get(receiver: T): Any? {
        return fieldReference.property.get(receiver)
    }
}
