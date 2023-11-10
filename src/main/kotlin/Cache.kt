import extensions.getSheetName
import kotlin.reflect.KClass

typealias EntityList = MutableList<Entity>
typealias KeyMap = MutableMap<Any?, Entity>

/** This class holds the caches for an [ExcelDB] */
class Cache {
    private val dataCache = mutableMapOf<KClass<out Entity>, EntityList>()
    private val keyCache = mutableMapOf<KClass<out Entity>, MutableMap<Any?, Entity>>()
    private val keyFieldReferenceCache = mutableMapOf<KClass<out Entity>, KeyFieldReference<out Entity>>()
    private val relatedEntities = mutableSetOf<KClass<Entity>>()
    private val kClassSheetMap = mutableMapOf<Pair<KClass<out Entity>, String?>, String>()

    /**
     * Returns the value for the given [kClass] if the value is present and not `null`.
     * Otherwise, calls the [defaultValue] function,
     * puts its result into the cache under the given key and returns the call result.
     */
    fun dataGetOrPut(kClass: KClass<out Entity>, defaultValue: () -> EntityList): EntityList =
        dataCache.getOrPut(kClass, defaultValue)

    /** Returns and remove the first element which is not equal to the given [kClass] from the related entities set */
    fun <T : Entity> popRelatedEntity(kClass: KClass<T>): KClass<Entity>? {
        return relatedEntities
            .firstOrNull { it != kClass }
            ?.also { relatedEntities.remove(it) }
    }

    /** Adds the [data] value to the cache.
     * The [action] will be executed when there is no cache entry for the class of data */
    fun <T : Entity> addEntity(data: T, action: (KClass<out T>) -> KeyMap): Any? {
        val kClass = data::class
        return getKeyFieldReference(kClass).get(data).let { key ->
            getEntityOrNull(kClass, key, action) ?: let {
                keyCache[kClass]!![key] = data
                dataCache[kClass]!!.add(data)
                @Suppress("UNCHECKED_CAST")
                kClass as KClass<Entity>
                if (relatedEntities.contains(kClass).not()) {
                    relatedEntities.add(kClass)
                }
            }
            key
        }
    }

    /**
     * Returns the value for the given [kClass] if the value is present and not `null`.
     * Otherwise, calls the [action] function,
     * puts its result into the cache under the given key and returns the call result.
     */
    fun <T : Entity> getEntityOrNull(kClass: KClass<T>, key: Any?, action: (KClass<T>) -> KeyMap): T? {
        @Suppress("UNCHECKED_CAST")
        return keyGetOrPut(kClass, action)[key] as T?
    }

    /** Returns the [KeyFieldReference] by the given [kClass] parameter */
    @Suppress("UNCHECKED_CAST")
    fun <T : Entity> getKeyFieldReference(kClass: KClass<out T>) =
        keyFieldReferenceGetOrPut(kClass) { KeyFieldReference(kClass) } as KeyFieldReference<T>

    /**
     * Returns the value for the given [kClass] if the value is present and not `null`.
     * Otherwise, puts the [sheetName] into the cache under the given key and returns the call result.
     */
    fun <T : Entity> sheetNameGetOrPut(kClass: KClass<T>, sheetName: String?) =
        kClassSheetMap.getOrPut(kClass to sheetName) { kClass.getSheetName(sheetName) }

    /** Clear data & key cache of the given [kClass] parameter */
    fun <T : Entity> clearData(kClass: KClass<T>) {
        keyCache[kClass] = mutableMapOf()
        dataCache[kClass] = mutableListOf()
    }

    private fun <T : Entity> keyGetOrPut(kClass: KClass<T>, defaultValue: (KClass<T>) -> KeyMap): KeyMap =
        keyCache.getOrPut(kClass) { defaultValue(kClass) }

    private fun keyFieldReferenceGetOrPut(
        kClass: KClass<out Entity>,
        defaultValue: () -> KeyFieldReference<out Entity>
    ): KeyFieldReference<out Entity> =
        keyFieldReferenceCache.getOrPut(kClass, defaultValue)
}
