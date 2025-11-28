// Evaluator.kt
import kotlin.math.floor

class Evaluator {

    private var environment = Environment()

    // Evaluate a single expression (for :evaluate command)
    fun evaluate(expr: Expr): Any? {
        return evalExpr(expr, environment)
    }

    // Evaluate a full program
    fun evaluateProgram(program: Program) {
        // Process imports (store them but don't do anything for now)
        for (importDecl in program.imports) {
            println("Imported: ${importDecl.name.lexeme}")
        }

        // Process heroes
        for (hero in program.heroes) {
            evaluateHero(hero)
        }

        // Process arena items
        for (item in program.arenaItems) {
            evaluateArenaItem(item)
        }

        // Process status effects
        for (effect in program.statusEffects) {
            evaluateStatusEffect(effect)
        }

        // Process items
        for (item in program.items) {
            evaluateItem(item)
        }

        // Process creeps
        for (creep in program.creeps) {
            evaluateCreep(creep)
        }
    }

    // ---- Hero Evaluation ----
    private fun evaluateHero(hero: Decl.HeroDecl) {
        println("\n=== Hero: ${hero.name.lexeme} ===")
        val heroEnv = Environment(environment)

        for (statement: HeroStatement in hero.statements) {
            when (statement) {
                is HeroStatement.SetStmt -> {
                    val value = evalExpr(statement.value, heroEnv)
                    heroEnv.define(statement.name, value)
                    println("  Set ${statement.name.lexeme} = ${stringify(value)}")
                }
                is HeroStatement.HeroStatBlock -> {
                    println("  Hero Stats:")
                    for (stat in statement.stats) {
                        val value = evalExpr(stat.value, heroEnv)
                        heroEnv.define(stat.name, value)
                        println("    ${stat.name.lexeme}: ${stringify(value)}")
                    }
                }
                is HeroStatement.ScalingCall -> {
                    println("  Scaling: ${statement.param1.lexeme} scales with ${statement.param2.lexeme}")
                }
                is HeroStatement.AbilitiesBlock -> {
                    println("  Abilities:")
                    for (ability in statement.abilities) {
                        evaluateAbility(ability, heroEnv)
                    }
                }
            }
        }
    }

    private fun evaluateAbility(ability: AbilityDecl, env: Environment) {
        println("    - ${ability.name.lexeme}")
        for (field: AbilityField in ability.fields) {
            when (field) {
                is AbilityField.TypeField ->
                    println("      Type: ${field.value.lexeme}")
                is AbilityField.CooldownField ->
                    println("      Cooldown: ${stringify(evalExpr(field.value, env))}")
                is AbilityField.ManaCostField ->
                    println("      Mana Cost: ${stringify(evalExpr(field.value, env))}")
                is AbilityField.RangeField ->
                    println("      Range: ${stringify(evalExpr(field.value, env))}")
                is AbilityField.DamageTypeField ->
                    println("      Damage Type: ${field.value.lexeme}")
                is AbilityField.BehaviorField ->
                    println("      Behavior: ${pipelineToString(field.pipeline, env)}")
            }
        }
    }

    // ---- Arena Evaluation ----
    private fun evaluateArenaItem(item: Decl) {
        when (item) {
            is Decl.TeamDecl -> {
                println("\n=== Team: ${item.name.lexeme} ===")
                environment.define(item.name, "Team")
            }
            is Decl.TurretDecl -> {
                println("\n=== Turret: ${item.name.lexeme} ===")
                for (stat in item.stats) {
                    val value = evalExpr(stat.value, environment)
                    println("  ${stat.name.lexeme}: ${stringify(value)}")
                }
            }
            is Decl.CoreDecl -> {
                println("\n=== Core: ${item.name.lexeme} ===")
                for (stat in item.stats) {
                    val value = evalExpr(stat.value, environment)
                    println("  ${stat.name.lexeme}: ${stringify(value)}")
                }
            }

            is Decl.ConstDecl -> TODO()
            is Decl.CreepDecl -> TODO()
            is Decl.HeroDecl -> TODO()
            is Decl.ImportDecl -> TODO()
            is Decl.ItemDecl -> TODO()
            is Decl.StatusEffectDecl -> TODO()
        }
    }

    // ---- Status Effect Evaluation ----
    private fun evaluateStatusEffect(effect: Decl.StatusEffectDecl) {
        println("\n=== Status Effect: ${effect.name.lexeme} ===")
        for (field: StatusEffectField in effect.fields) {
            when (field) {
                is StatusEffectField.TypeField ->
                    println("  Type: ${field.value.lexeme}")
                is StatusEffectField.DurationField ->
                    println("  Duration: ${stringify(evalExpr(field.value, environment))}")
                is StatusEffectField.OnApplyField ->
                    println("  On Apply: ${field.block.statements.size} statement(s)")
                is StatusEffectField.OnTickField ->
                    println("  On Tick: ${field.block.statements.size} statement(s)")
            }
        }
    }

    // ---- Item Evaluation ----
    private fun evaluateItem(item: Decl.ItemDecl) {
        println("\n=== Item: ${item.name.lexeme} ===")
        for (field: ItemField in item.fields) {
            when (field) {
                is ItemField.PropertyField -> {
                    val value = evalExpr(field.value, environment)
                    println("  ${field.name.lexeme}: ${stringify(value)}")
                }
                is ItemField.EffectField ->
                    println("  Effect: ${pipelineToString(field.pipeline, environment)}")
            }
        }
    }

    // ---- Creep Evaluation ----
    private fun evaluateCreep(creep: Decl.CreepDecl) {
        println("\n=== Creep: ${creep.name.lexeme} ===")
        for (stat in creep.stats) {
            val value = evalExpr(stat.value, environment)
            println("  ${stat.name.lexeme}: ${stringify(value)}")
        }
    }

    // ---- Expression Evaluation ----
    private fun evalExpr(expr: Expr, env: Environment): Any? = when (expr) {
        is Expr.Literal -> expr.value

        is Expr.Variable -> env.get(expr.name)

        is Expr.Grouping -> evalExpr(expr.expression, env)

        is Expr.Binary -> {
            val left = evalExpr(expr.left, env)
            val right = evalExpr(expr.right, env)
            evalBinary(expr.operator, left, right)
        }

        is Expr.Percentage -> expr.value / 100.0

        is Expr.Time -> expr.seconds.toDouble()

        is Expr.FunctionCallExpr -> {
            // Return a string representation for now
            functionCallToString(expr.call, env)
        }
    }

    private fun evalBinary(operator: Token, left: Any?, right: Any?): Any? {
        return when (operator.type) {
            // Arithmetic
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeException("Operands must be two numbers or two strings")
                }
            }

            TokenType.MINUS -> {
                val a = expectNumber(operator, left)
                val b = expectNumber(operator, right)
                a - b
            }

            TokenType.STAR -> {
                val a = expectNumber(operator, left)
                val b = expectNumber(operator, right)
                a * b
            }

            TokenType.DIVIDE -> {
                val a = expectNumber(operator, left)
                val b = expectNumber(operator, right)
                if (b == 0.0) {
                    throw RuntimeException("Division by zero")
                }
                a / b
            }

            // Comparisons
            TokenType.LESS -> {
                val a = expectNumber(operator, left)
                val b = expectNumber(operator, right)
                a < b
            }

            TokenType.LESS_EQUAL -> {
                val a = expectNumber(operator, left)
                val b = expectNumber(operator, right)
                a <= b
            }

            TokenType.GREATER -> {
                val a = expectNumber(operator, left)
                val b = expectNumber(operator, right)
                a > b
            }

            TokenType.GREATER_EQUAL -> {
                val a = expectNumber(operator, left)
                val b = expectNumber(operator, right)
                a >= b
            }

            // Equality
            TokenType.EQUAL_EQUAL -> left == right
            TokenType.BANG_EQUAL -> left != right

            else -> throw RuntimeException("Unsupported operator: ${operator.lexeme}")
        }
    }

    // ---- Helper Methods ----
    private fun expectNumber(operator: Token, value: Any?): Double {
        if (value is Double) return value
        if (value is Int) return value.toDouble()
        throw RuntimeException("Operand must be a number (at ${operator.lexeme})")
    }

    private fun stringify(value: Any?): String = when (value) {
        null -> "nil"
        is String -> value
        is Double -> if (value == floor(value)) value.toInt().toString() else value.toString()
        is Boolean -> value.toString()
        else -> value.toString()
    }

    private fun pipelineToString(pipeline: PipelineExpr, env: Environment): String =
        pipeline.calls.joinToString(" |> ") { functionCallToString(it, env) }

    private fun functionCallToString(call: FunctionCall, env: Environment): String {
        val args = call.arguments.joinToString(", ") { argumentToString(it, env) }
        return "${call.name.lexeme}($args)"
    }

    private fun argumentToString(arg: Argument, env: Environment): String = when (arg) {
        is Argument.NamedArg -> "${arg.name.lexeme}: ${exprToString(arg.value, env)}"
        is Argument.PositionalArg -> exprToString(arg.value, env)
    }

    private fun exprToString(expr: Expr, env: Environment): String = when (expr) {
        is Expr.Literal -> stringify(expr.value)
        is Expr.Variable -> expr.name.lexeme
        is Expr.Percentage -> "${expr.value}%"
        is Expr.Time -> "${expr.seconds}s"
        is Expr.Binary -> "(${expr.operator.lexeme} ${exprToString(expr.left, env)} ${exprToString(expr.right, env)})"
        is Expr.Grouping -> "(${exprToString(expr.expression, env)})"
        is Expr.FunctionCallExpr -> functionCallToString(expr.call, env)
    }
}