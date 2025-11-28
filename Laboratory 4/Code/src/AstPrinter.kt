// AstPrinter.kt
import kotlin.math.floor

class AstPrinter {

    fun print(program: Program) {
        println(astToString(program))
    }

    private fun astToString(program: Program): String = buildString {
        appendLine("(Program")

        if (program.imports.isNotEmpty()) {
            appendLine("  (Imports")
            program.imports.forEach { appendLine("    ${importToString(it)}") }
            appendLine("  )")
        }

        if (program.heroes.isNotEmpty()) {
            appendLine("  (Heroes")
            program.heroes.forEach { appendLine(indent(heroToString(it), 4)) }
            appendLine("  )")
        }

        if (program.arenaItems.isNotEmpty()) {
            appendLine("  (Arena")
            program.arenaItems.forEach { appendLine(indent(arenaItemToString(it), 4)) }
            appendLine("  )")
        }

        if (program.statusEffects.isNotEmpty()) {
            appendLine("  (StatusEffects")
            program.statusEffects.forEach { appendLine(indent(statusEffectToString(it), 4)) }
            appendLine("  )")
        }

        if (program.items.isNotEmpty()) {
            appendLine("  (Items")
            program.items.forEach { appendLine(indent(itemToString(it), 4)) }
            appendLine("  )")
        }

        if (program.creeps.isNotEmpty()) {
            appendLine("  (Creeps")
            program.creeps.forEach { appendLine(indent(creepToString(it), 4)) }
            appendLine("  )")
        }

        append(")")
    }

    private fun importToString(decl: Decl.ImportDecl) = "(Import ${decl.name.lexeme})"

    private fun heroToString(hero: Decl.HeroDecl): String = buildString {
        appendLine("(Hero ${hero.name.lexeme}")
        hero.statements.forEach { appendLine(indent(heroStatementToString(it), 2)) }
        append(")")
    }

    private fun heroStatementToString(stmt: HeroStatement): String = when (stmt) {
        is HeroStatement.SetStmt -> "(Set ${stmt.name.lexeme} = ${exprToString(stmt.value)})"
        is HeroStatement.HeroStatBlock -> buildString {
            appendLine("(HeroStat")
            stmt.stats.forEach { appendLine(indent(statEntryToString(it), 2)) }
            append(")")
        }
        is HeroStatement.ScalingCall -> "(Scaling ${stmt.param1.lexeme} ${stmt.param2.lexeme})"
        is HeroStatement.AbilitiesBlock -> buildString {
            appendLine("(Abilities")
            stmt.abilities.forEach { appendLine(indent(abilityToString(it), 2)) }
            append(")")
        }
    }

    private fun abilityToString(ability: AbilityDecl): String = buildString {
        appendLine("(Ability ${ability.name.lexeme}")
        ability.fields.forEach { appendLine(indent(abilityFieldToString(it), 2)) }
        append(")")
    }

    private fun abilityFieldToString(field: AbilityField): String = when (field) {
        is AbilityField.TypeField -> "(Type ${field.value.lexeme})"
        is AbilityField.CooldownField -> "(Cooldown ${exprToString(field.value)})"
        is AbilityField.ManaCostField -> "(ManaCost ${exprToString(field.value)})"
        is AbilityField.RangeField -> "(Range ${exprToString(field.value)})"
        is AbilityField.DamageTypeField -> "(DamageType ${field.value.lexeme})"
        is AbilityField.BehaviorField -> "(Behavior ${pipelineToString(field.pipeline)})"
    }

    private fun pipelineToString(pipeline: PipelineExpr): String =
        pipeline.calls.joinToString(" |> ") { functionCallToString(it) }

    private fun functionCallToString(call: FunctionCall): String {
        val args = call.arguments.joinToString(", ") { argumentToString(it) }
        return "${call.name.lexeme}($args)"
    }

    private fun argumentToString(arg: Argument): String = when (arg) {
        is Argument.NamedArg -> "${arg.name.lexeme}: ${exprToString(arg.value)}"
        is Argument.PositionalArg -> exprToString(arg.value)
    }

    private fun statEntryToString(entry: StatEntry) =
        "(${entry.name.lexeme}: ${exprToString(entry.value)})"

    private fun arenaItemToString(decl: Decl): String = when (decl) {
        is Decl.TeamDecl -> "(Team ${decl.name.lexeme})"
        is Decl.TurretDecl -> buildString {
            appendLine("(Turret ${decl.name.lexeme}")
            decl.stats.forEach { appendLine(indent(statEntryToString(it), 2)) }
            append(")")
        }
        is Decl.CoreDecl -> buildString {
            appendLine("(Core ${decl.name.lexeme}")
            decl.stats.forEach { appendLine(indent(statEntryToString(it), 2)) }
            append(")")
        }
        else -> "(Unknown)"
    }

    private fun statusEffectToString(effect: Decl.StatusEffectDecl): String = buildString {
        appendLine("(StatusEffect ${effect.name.lexeme}")
        effect.fields.forEach { appendLine(indent(statusEffectFieldToString(it), 2)) }
        append(")")
    }

    private fun statusEffectFieldToString(field: StatusEffectField): String = when (field) {
        is StatusEffectField.TypeField -> "(Type ${field.value.lexeme})"
        is StatusEffectField.DurationField -> "(Duration ${exprToString(field.value)})"
        is StatusEffectField.OnApplyField -> "(OnApply ${blockToString(field.block)})"
        is StatusEffectField.OnTickField -> "(OnTick ${blockToString(field.block)})"
    }

    private fun blockToString(block: BlockStmt): String = buildString {
        append("{ ")
        append(block.statements.joinToString(" ") { stmtToString(it) })
        append(" }")
    }

    private fun stmtToString(stmt: Stmt): String = when (stmt) {
        is Stmt.ApplyStmt -> "(Apply ${functionCallToString(stmt.call)} to ${targetToString(stmt.target)})"
        is Stmt.PipelineStmt -> "(Pipeline ${pipelineToString(stmt.pipeline)})"
        is Stmt.SetStmt -> "(Set ${stmt.name.lexeme} = ${exprToString(stmt.value)})"
        is Stmt.StatEntryStmt -> statEntryToString(stmt.entry)
        is Stmt.FunctionCallStmt -> functionCallToString(stmt.call)
    }

    private fun targetToString(target: TargetExpr): String = when (target) {
        is TargetExpr.Self -> "self"
        is TargetExpr.Target -> "target"
        is TargetExpr.Caster -> "caster"
        is TargetExpr.Named -> target.name.lexeme
    }

    private fun itemToString(item: Decl.ItemDecl): String = buildString {
        appendLine("(Item ${item.name.lexeme}")
        item.fields.forEach { appendLine(indent(itemFieldToString(it), 2)) }
        append(")")
    }

    private fun itemFieldToString(field: ItemField): String = when (field) {
        is ItemField.PropertyField -> "(${field.name.lexeme}: ${exprToString(field.value)})"
        is ItemField.EffectField -> "(Effect ${pipelineToString(field.pipeline)})"
    }

    private fun creepToString(creep: Decl.CreepDecl): String = buildString {
        appendLine("(Creep ${creep.name.lexeme}")
        creep.stats.forEach { appendLine(indent(statEntryToString(it), 2)) }
        append(")")
    }

    private fun exprToString(expr: Expr): String = when (expr) {
        is Expr.Literal -> literalToString(expr.value)
        is Expr.Variable -> expr.name.lexeme
        is Expr.Binary -> "(${expr.operator.lexeme} ${exprToString(expr.left)} ${exprToString(expr.right)})"
        is Expr.Grouping -> "(group ${exprToString(expr.expression)})"
        is Expr.FunctionCallExpr -> functionCallToString(expr.call)
        is Expr.Percentage -> "${expr.value}%"
        is Expr.Time -> "${expr.seconds}s"
    }

    private fun literalToString(v: Any?): String = when (v) {
        null -> "nil"
        is String -> "\"$v\""
        is Double -> if (v == floor(v)) "${v.toInt()}" else v.toString()
        else -> v.toString()
    }

    private fun indent(text: String, spaces: Int): String {
        val prefix = " ".repeat(spaces)
        return text.lines().joinToString("\n") { prefix + it }
    }
}