// ExecutionContext.kt
data class ExecutionContext(
    val world: World,
    val self: Entity? = null,
    val target: Entity? = null,
    val caster: Entity? = null
) {

    fun resolveTarget(expr: TargetExpr): Entity? {
        return when (expr) {
            TargetExpr.Self -> self
            TargetExpr.Target -> target
            TargetExpr.Caster -> caster
            is TargetExpr.Named -> world.getEntity(expr.name.lexeme)
        }
    }
}
