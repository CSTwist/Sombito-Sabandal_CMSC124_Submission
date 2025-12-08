// AstPrinter.kt
//
// Pretty-printer for the complete AST:
// Program → Decls → Stmts → Expr
//
// This version matches Decl.kt, Expr.kt, Stmt.kt, and Program.kt exactly.
//

class AstPrinter {

    fun print(program: Program) {
        println("Program: ${program.name.lexeme}")

        printImports(program)
        printVariables(program)
        printHeroes(program)
        printArena(program)
        printStatusEffects(program)
        printItems(program)
        printCreeps(program)
        printFunctions(program)
    }

    // ---------------------------------------------------------
    // IMPORTS
    // ---------------------------------------------------------
    private fun printImports(program: Program) {
        println("Imports:")
        program.imports.forEach {
            println("  import ${it.name.lexeme}")
        }
    }

    // ---------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------
    private fun printVariables(program: Program) {
        println("Variables:")
        program.variables.forEach {
            println("  ${it.name.lexeme} = ${exprToString(it.value)}")
        }
    }

    // ---------------------------------------------------------
    // HEROES
    // ---------------------------------------------------------
    private fun printHeroes(program: Program) {
        println("Heroes:")
        program.heroes.forEach { hero ->
            println("  Hero ${hero.name.lexeme}:")
            hero.statements.forEach { stmt ->
                println("    ${heroStmtToString(stmt)}")
            }
        }
    }

    private fun heroStmtToString(stmt: HeroStatement): String = when (stmt) {
        is HeroStatement.SetStmt ->
            "set ${stmt.name.lexeme} = ${exprToString(stmt.value)}"

        is HeroStatement.HeroStatBlock ->
            "heroStat { ${stmt.stats.joinToString { "${it.name.lexeme}: ${exprToString(it.value)}" }} }"

        is HeroStatement.AbilitiesBlock ->
            "abilities { ${stmt.abilities.joinToString { abilityToString(it) }} }"
    }

    private fun abilityToString(ability: AbilityDecl): String {
        val fields = ability.fields.joinToString { abilityFieldToString(it) }
        return "ability ${ability.name.lexeme} { $fields }"
    }

    private fun abilityFieldToString(field: AbilityField): String = when (field) {
        is AbilityField.TypeField -> "type: ${field.value.lexeme}"
        is AbilityField.CooldownField -> "cooldown: ${exprToString(field.value)}"
        is AbilityField.ManaCostField -> "mana_cost: ${exprToString(field.value)}"
        is AbilityField.RangeField -> "range: ${exprToString(field.value)}"
        is AbilityField.DamageTypeField -> "damage_type: ${field.value.lexeme}"
        is AbilityField.BehaviorField -> when {
            field.body != null -> "behavior { ${blockToString(field.body)} }"
            field.expression != null -> "behavior ${exprToString(field.expression)}"
            else -> "behavior <empty>"
        }
    }

    // ---------------------------------------------------------
    // ARENA
    // ---------------------------------------------------------
    private fun printArena(program: Program) {
        println("Arena Objects:")
        program.arenaItems.forEach { decl ->
            when (decl) {
                is Decl.TurretDecl ->
                    println("  turret ${decl.name.lexeme} { ${statsList(decl.stats)} }")

                is Decl.CoreDecl ->
                    println("  core ${decl.name.lexeme} { ${statsList(decl.stats)} }")

                else -> {}
            }
        }

        println("Teams:")
        program.teams.forEach { team ->
            println("  Team ${team.name.lexeme}:")
            println("    Core = ${team.coreRef?.lexeme ?: "None"}")
            println("    Turrets:")
            team.turrets.forEach {
                println("      turret ${it.name.lexeme} { ${statsList(it.stats)} }")
            }
        }
    }

    private fun statsList(stats: List<StatEntry>): String =
        stats.joinToString { "${it.name.lexeme}: ${exprToString(it.value)}" }

    // ---------------------------------------------------------
    // STATUS EFFECTS
    // ---------------------------------------------------------
    private fun printStatusEffects(program: Program) {
        println("Status Effects:")
        program.statusEffects.forEach { se ->
            println("  statusEffect ${se.name.lexeme} {")
            se.fields.forEach { field ->
                println("    ${statusEffectFieldToString(field)}")
            }
            println("  }")
        }
    }

    private fun statusEffectFieldToString(f: StatusEffectField): String = when (f) {
        is StatusEffectField.TypeField -> "type: ${f.value.lexeme}"
        is StatusEffectField.DurationField -> "duration: ${exprToString(f.value)}"
        is StatusEffectField.OnApplyField -> "on_apply { ${blockToString(f.block)} }"
        is StatusEffectField.OnTickField -> "on_tick { ${blockToString(f.block)} }"
        is StatusEffectField.OnExpireField -> "on_expire { ${blockToString(f.block)} }"
    }

    // ---------------------------------------------------------
    // ITEMS
    // ---------------------------------------------------------
    private fun printItems(program: Program) {
        println("Items:")
        program.items.forEach { item ->
            println("  item ${item.name.lexeme} {")
            item.fields.forEach { field ->
                println("    ${itemFieldToString(field)}")
            }
            println("  }")
        }
    }

    private fun itemFieldToString(field: ItemField): String = when (field) {
        is ItemField.PropertyField -> "${field.name.lexeme}: ${exprToString(field.value)}"
        is ItemField.PassiveField -> "passive: ${exprToString(field.behavior)}"
    }

    // ---------------------------------------------------------
    // CREEPS
    // ---------------------------------------------------------
    private fun printCreeps(program: Program) {
        println("Creeps:")
        program.creeps.forEach { creep ->
            println("  creep ${creep.name.lexeme} { ${statsList(creep.stats)} }")
        }
    }

    // ---------------------------------------------------------
    // FUNCTIONS
    // ---------------------------------------------------------
    private fun printFunctions(program: Program) {
        println("Functions:")
        program.functions.forEach { fn ->
            val paramStr = fn.params.joinToString { "${it.type.lexeme} ${it.name.lexeme}" }
            println("  function ${fn.name.lexeme}($paramStr) { ${blockToString(fn.body)} }")
        }
    }

    // ---------------------------------------------------------
    // EXPRESSIONS
    // ---------------------------------------------------------
    private fun exprToString(expr: Expr): String = when (expr) {

        is Expr.Literal -> expr.value.toString()
        is Expr.Percentage -> "${expr.value}%"
        is Expr.Time -> "${expr.seconds}s"
        is Expr.Variable -> expr.name.lexeme
        is Expr.Grouping -> "( ${exprToString(expr.expression)} )"

        is Expr.Unary ->
            "(${expr.operator.lexeme}${exprToString(expr.right)})"

        is Expr.Binary ->
            "(${exprToString(expr.left)} ${expr.operator.lexeme} ${exprToString(expr.right)})"

        is Expr.Logical ->
            "(${exprToString(expr.left)} ${expr.operator.lexeme} ${exprToString(expr.right)})"

        is Expr.FunctionCallExpr ->
            functionCallToString(expr.call)
    }

    private fun functionCallToString(call: FunctionCall): String {
        val args = call.arguments.joinToString {
            when (it) {
                is Argument.NamedArg -> "${it.name.lexeme}: ${exprToString(it.value)}"
                is Argument.PositionalArg -> exprToString(it.value)
            }
        }
        return "${call.name.lexeme}($args)"
    }

    // ---------------------------------------------------------
    // STATEMENTS
    // ---------------------------------------------------------
    private fun blockToString(block: BlockStmt): String =
        block.statements.joinToString("; ") { stmtToString(it) }

    private fun stmtToString(stmt: Stmt): String = when (stmt) {

        is Stmt.ExprStmt ->
            exprToString(stmt.expr)

        is Stmt.SetStmt ->
            "${stmt.name.lexeme} = ${exprToString(stmt.value)}"

        is Stmt.ConstDeclStmt ->
            "const ${stmt.type.lexeme} ${stmt.name.lexeme} = ${exprToString(stmt.value)}"

        is Stmt.StatEntryStmt ->
            "${stmt.entry.name.lexeme}: ${exprToString(stmt.entry.value)}"

        is Stmt.FunctionCallStmt ->
            functionCallToString(stmt.call)

        is Stmt.ApplyStmt ->
            "apply ${functionCallToString(stmt.call)} to ${targetToString(stmt.target)}"

        is Stmt.IfStmt ->
            "if (${exprToString(stmt.condition)}) { ${blockToString(stmt.thenBranch)} }" +
                    (stmt.elseBranch?.let { " else { ${blockToString(it)} }" } ?: "")

        is Stmt.WhileStmt ->
            "while (${exprToString(stmt.condition)}) { ${blockToString(stmt.body)} }"

        is Stmt.ForStmt ->
            "for (${stmt.variable.lexeme} in ${exprToString(stmt.collection)}) { ${blockToString(stmt.body)} }"

        is Stmt.ReturnStmt ->
            "return ${stmt.value?.let { exprToString(it) } ?: ""}"

        is Stmt.FunStmt ->
            "fun ${stmt.variable.lexeme}"
    }

    private fun targetToString(target: TargetExpr): String = when (target) {
        TargetExpr.Self -> "self"
        TargetExpr.Target -> "target"
        TargetExpr.Caster -> "caster"
        is TargetExpr.Named -> target.name.lexeme
    }
}
