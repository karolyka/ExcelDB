import extensions.asEntity
import extensions.fieldName
import extensions.getKeyField
import extensions.normalizeFieldName
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

/**
 *  This class holds the references of a field
 *
 *  @param T          An [Entity]
 *  @param kClass     [KClass] of the [Entity]
 *  @param kParameter A constructor parameter
 *  @property name    Name of the field/column
 *  */
class FieldReference<T : Entity>(
    private val kClass: KClass<T>,
    private val kParameter: KParameter = kClass.getKeyField(),
    val name: String = kParameter.fieldName
) {
    private val normalizedName by lazy { name.normalizeFieldName() }
    private val asEntity by lazy { kParameter.asEntity }

    /** The related property for the given [kParameter] */
    val property by lazy { kClass.memberProperties.first { it.name == name } }

    /** Returns `true` when the [FieldReference] is an [Entity] */
    val isEntity = asEntity != null

    /** Returns `true` when the given [fieldName] is match to the [name] or to the [normalizeFieldName] name */
    fun isEqual(fieldName: String): Boolean =
        name.equals(fieldName, true) ||
            normalizedName.equals(fieldName, true)
}
