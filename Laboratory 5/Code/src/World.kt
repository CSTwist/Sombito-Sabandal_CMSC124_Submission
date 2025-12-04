// World.kt
class World {

    private val entities = mutableMapOf<String, Entity>()
    val functions = mutableMapOf<String, NativeFunction>()

    fun registerEntity(entity: Entity) {
        entities[entity.name] = entity
    }

    fun getEntity(name: String): Entity? {
        return entities[name]
    }

    fun getOrCreateEntity(name: String): Entity {
        return entities.getOrPut(name) { Entity(name) }
    }

    fun dump() {
        println("=== WORLD STATE ===")
        for (e in entities.values) {
            println(e)
        }
    }
}
