// Evaluator.kt
import kotlin.math.floor

class Evaluator {

    private var environment = Environment()

    // ============================================================
    // Evaluate a single expression (for :evaluate command)
    // ============================================================
    fun evaluate(expr: Expr): Any? {
        return evalExpr(expr, environment)
    }

    // ============================================================
    // Evaluate a full program
    // ============================================================
    fun evaluateProgram(program: Program) {
        // Imports (store / log for now)
        for (importDecl in program.imports) {
            println("Imported: ${importDecl.name.lexeme}")
        }

        // Global variables
        println("\n=== Global Variables ===")
        for (varDecl in program.variables) {
            val value = evalExpr(varDecl.value, environment)
            environment.define(varDecl.name, value)
            println("Set ${varDecl.name.lexeme} = ${stringify(value)}")
        }

        // Heroes
        for (hero in program.heroes) {
            evaluateHero(hero)
        }

        // Arena items
        for (item in program.arenaItems) {
            evaluateArenaItem(item)
        }

        // Status effects
        for (effect in program.statusEffects) {
            evaluateStatusEffect(effect)
        }

        // Items
        for (item in program.items) {
            evaluateItem(item)
        }

        // Creeps
        for (creep in program.creeps) {
            evaluateCreep(creep)
        }
    }

    // ============================================================
    //                        HEROES
    // ============================================================
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

                is AbilityField.BehaviorField -> {
                    println("      Behavior block:")
                    // Execute behavior in a fresh child environment of this hero/ability
                    val behaviorEnv = Environment(env)
                    executeBlock(field.body, behaviorEnv, indent = "        ")
                }
            }
        }
    }

    // ============================================================
    //                        ARENA
    // ============================================================
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
            is Decl.ConstDecl -> {
                println("\n=== Const: ${item.name.lexeme} ===")
                val value = evalExpr(item.value, environment)
                environment.define(item.name, value)
                println("  Value: ${stringify(value)}")
            }
            is Decl.VarDecl -> {
                val value = evalExpr(item.value, environment)
                environment.define(item.name, value)
                println("  Set ${item.name.lexeme} = ${stringify(value)}")
            }
            is Decl.HeroDecl -> {
                evaluateHero(item)
            }
            is Decl.CreepDecl -> {
                evaluateCreep(item)
            }
            is Decl.ItemDecl -> {
                evaluateItem(item)
            }
            is Decl.StatusEffectDecl -> {
                evaluateStatusEffect(item)
            }
            is Decl.ImportDecl -> {
                println("  Imported: ${item.name.lexeme}")
            }
            is Decl.FunctionDecl -> {
                // Not yet used at top-level, but you could store function definitions here later.
                println("\n=== Function Decl: ${item.name.lexeme} (...) ===")
            }
        }
    }

    // ============================================================
    //                    STATUS EFFECTS
    // ============================================================
    private fun evaluateStatusEffect(effect: Decl.StatusEffectDecl) {
        println("\n=== Status Effect: ${effect.name.lexeme} ===")
        for (field: StatusEffectField in effect.fields) {
            when (field) {
                is StatusEffectField.TypeField ->
                    println("  Type: ${field.value.lexeme}")

                is StatusEffectField.DurationField ->
                    println("  Duration: ${stringify(evalExpr(field.value, environment))}")

                is StatusEffectField.OnApplyField -> {
                    println("  On Apply:")
                    val applyEnv = Environment(environment)
                    executeBlock(field.block, applyEnv, indent = "    ")
                }

                is StatusEffectField.OnTickField -> {
                    println("  On Tick:")
                    val tickEnv = Environment(environment)
                    executeBlock(field.block, tickEnv, indent = "    ")
                }
            }
        }
    }

    // ============================================================
    //                           ITEMS
    // ============================================================
    private fun evaluateItem(item: Decl.ItemDecl) {
        println("\n=== Item: ${item.name.lexeme} ===")
        for (field: ItemField in item.fields) {
            when (field) {
                is ItemField.PropertyField -> {
                    val value = evalExpr(field.value, environment)
                    println("  ${field.name.lexeme}: ${stringify(value)}")
                }
                is ItemField.EffectField -> {
                    println("  Effect block:")
                    val effectEnv = Environment(environment)
                    executeBlock(field.body, effectEnv, indent = "    ")
                }
            }
        }
    }

    // ============================================================
    //                           CREEPS
    // ============================================================
    private fun evaluateCreep(creep: Decl.CreepDecl) {
        println("\n=== Creep: ${creep.name.lexeme} ===")
        for (stat in creep.stats) {
            val value = evalExpr(stat.value, environment)
            println("  ${stat.name.lexeme}: ${stringify(value)}")
        }
    }

    // ============================================================
    //                       STATEMENTS
    // ============================================================
    private fun executeBlock(block: BlockStmt, env: Environment, indent: String = "") {
        for (stmt in block.statements) {
            execute(stmt, env, indent)
        }
    }

    private fun execute(stmt: Stmt, env: Environment, indent: String = "") {
        when (stmt) {
            is Stmt.ApplyStmt -> {
                val callStr = functionCallToString(stmt.call, env)
                val targetStr = targetToString(stmt.target)
                println("${indent}Apply $callStr to $targetStr")
                // Hook into engine here later if needed
            }

            is Stmt.SetStmt -> {
                val value = evalExpr(stmt.value, env)
                // define or assign depending on existence
                if (env.isDefinedAnywhere(stmt.name)) {
                    env.assign(stmt.name, value)
                } else {
                    env.define(stmt.name, value)
                }
                println("${indent}Set ${stmt.name.lexeme} = ${stringify(value)}")
            }

            is Stmt.StatEntryStmt -> {
                val value = evalExpr(stmt.entry.value, env)
                env.define(stmt.entry.name, value)
                println("${indent}Stat ${stmt.entry.name.lexeme} = ${stringify(value)}")
            }

            is Stmt.FunctionCallStmt -> {
                val result = evalExpr(Expr.FunctionCallExpr(stmt.call), env)
                println("${indent}Call ${functionCallToString(stmt.call, env)} -> $result")
            }

            is Stmt.ExprStmt -> {
                val result = evalExpr(stmt.expr, env)
                println("${indent}Expr ${exprToString(stmt.expr, env)} -> ${stringify(result)}")
            }

            is Stmt.IfStmt -> {
                val cond = evalExpr(stmt.condition, env)
                if (isTruthy(cond)) {
                    val child = Environment(env)
                    println("${indent}If ${exprToString(stmt.condition, env)} then:")
                    executeBlock(stmt.thenBranch, child, indent + "  ")
                } else if (stmt.elseBranch != null) {
                    val child = Environment(env)
                    println("${indent}Else:")
                    executeBlock(stmt.elseBranch, child, indent + "  ")
                }
            }

            is Stmt.WhileStmt -> {
                println("${indent}While loop:")
                while (isTruthy(stmt.condition?.let { evalExpr(it, env) } ?: true)) {
                    val bodyEnv = Environment(env)
                    executeBlock(stmt.body, bodyEnv, indent + "  ")
                }
            }

            is Stmt.ForStmt -> {
                println("${indent}For loop:")
                val loopEnv = Environment(env)
                stmt.initializer?.let { execute(it, loopEnv, indent + "  ") }
                while (stmt.condition?.let { isTruthy(evalExpr(it, loopEnv)) } ?: true) {
                    executeBlock(stmt.body, loopEnv, indent + "  ")
                    stmt.increment?.let { execute(it, loopEnv, indent + "  ") }
                }
            }

            is Stmt.ReturnStmt -> {
                val value = stmt.value?.let { evalExpr(it, env) }
                println("${indent}Return ${value?.let { stringify(it) } ?: "nil"} (ignored, functions not yet implemented)")
                // Proper returns would throw a signal/exception in a function context.
            }

            is Stmt.FunStmt -> {
                println("${indent}Function '${stmt.name.lexeme}' declared (body not yet callable)")
                // Later you can store this in env as a callable value.
            }
        }
    }

    private fun targetToString(target: TargetExpr): String = when (target) {
        is TargetExpr.Self -> "self"
        is TargetExpr.Target -> "target"
        is TargetExpr.Caster -> "caster"
        is TargetExpr.Named -> target.name.lexeme
    }

    // ============================================================
    //                    EXPRESSION EVALUATION
    // ============================================================
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
            // For now, just return a string representation
            functionCallToString(expr.call, env)
        }

        is Expr.Logical -> evalLogical(expr, env)

        is Expr.Unary -> evalUnary(expr, env)
    }

    private fun evalLogical(expr: Expr.Logical, env: Environment): Any? {
        val left = evalExpr(expr.left, env)

        return when (expr.operator.type) {
            TokenType.OR -> {
                if (isTruthy(left)) left else evalExpr(expr.right, env)
            }
            TokenType.AND -> {
                if (!isTruthy(left)) left else evalExpr(expr.right, env)
            }
            else -> throw RuntimeException("Invalid logical operator ${expr.operator.lexeme}")
        }
    }

    private fun evalUnary(expr: Expr.Unary, env: Environment): Any? {
        val right = evalExpr(expr.right, env)
        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> -expectNumber(expr.operator, right)
            else -> throw RuntimeException("Invalid unary operator ${expr.operator.lexeme}")
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
                } else if (left is String || right is String) {
                    stringify(left) + stringify(right)
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

    // ============================================================
    //                        HELPERS
    // ============================================================
    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        if (value is Boolean) return value
        return true
    }

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
        is Expr.Logical -> "(${expr.operator.lexeme} ${exprToString(expr.left, env)} ${exprToString(expr.right, env)})"
        is Expr.Unary -> "(${expr.operator.lexeme}${exprToString(expr.right, env)})"
    }
}
