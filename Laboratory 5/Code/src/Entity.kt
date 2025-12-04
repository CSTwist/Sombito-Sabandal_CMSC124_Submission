// Entity.kt
data class Entity(
    val name: String,
    val stats: MutableMap<String, Double> = mutableMapOf(),
    val statusEffects: MutableList<AppliedStatusEffect> = mutableListOf()
) {

    fun getStat(key: String): Double {
        return stats[key] ?: 0.0
    }

    fun setStat(key: String, value: Double) {
        stats[key] = value
    }

    fun modifyStat(key: String, delta: Double) {
        stats[key] = getStat(key) + delta
    }

    override fun toString(): String {
        return "Entity($name, stats=$stats)"
    }
}

data class AppliedStatusEffect(
    val name: String,
    var duration: Double,
    val onTick: ((Entity) -> Unit)? = null
)
