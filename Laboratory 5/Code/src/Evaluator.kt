// Evaluator.kt
import kotlin.math.floor

// ============================================================
// Helper Definitions
// ============================================================
interface NativeFunction {
    fun name(): String
    fun arity(): Int
    fun call(context: ExecutionContext, args: List<Any?>): Any?
}

class ExecutionContext(val world: World)
class World

class Evaluator {

    // 1. Buffer to store the logs instead of printing immediately
    private val logBuffer = StringBuilder()

    // Helper to append to buffer
    private fun log(message: String) {
        logBuffer.appendLine(message)
    }

    private var environment = Environment().apply {
        // 2. Register the built-in 'printLog' function
        val printLogToken = Token(TokenType.IDENTIFIER, "printLog", null, 0)
        define(printLogToken, PrintLog(logBuffer))
    }

    // ============================================================
    // Native Function Implementation
    // ============================================================
    class PrintLog(private val buffer: StringBuilder) : NativeFunction {
        override fun name() = "printLog"
        override fun arity() = 0
        override fun call(context: ExecutionContext, args: List<Any?>): Any? {
            // This flushes the buffer to the real STDOUT when the user calls printLog()
            print(buffer.toString())
            return null
        }
    }

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
        // Imports
        for (importDecl in program.imports) {
            log("Imported: ${importDecl.name.lexeme}")
        }

        // Global variables (Declarations and Reassignments)
        log("\n=== Global Variables ===")
        for (varDecl in program.variables) {
            val value = evalExpr(varDecl.value, environment)
            // We use 'define' here which acts as an upsert (create or update)
            // This handles both 'set x = 1;' and 'x = 2;' at the top level.
            environment.define(varDecl.name, value)
            log("Set ${varDecl.name.lexeme} = ${stringify(value)}")
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
        log("\n=== Hero: ${hero.name.lexeme} ===")
        val heroEnv = Environment(environment)

        for (statement: HeroStatement in hero.statements) {
            when (statement) {
                is HeroStatement.SetStmt -> {
                    val value = evalExpr(statement.value, heroEnv)
                    heroEnv.define(statement.name, value)
                    log("  Set ${statement.name.lexeme} = ${stringify(value)}")
                }
                is HeroStatement.HeroStatBlock -> {
                    log("  Hero Stats:")
                    for (stat in statement.stats) {
                        val value = evalExpr(stat.value, heroEnv)
                        heroEnv.define(stat.name, value)
                        log("    ${stat.name.lexeme}: ${stringify(value)}")
                    }
                }
                is HeroStatement.AbilitiesBlock -> {
                    log("  Abilities:")
                    for (ability in statement.abilities) {
                        evaluateAbility(ability, heroEnv)
                    }
                }
            }
        }
    }

    private fun evaluateAbility(ability: AbilityDecl, env: Environment) {
        log("    - ${ability.name.lexeme}")
        for (field: AbilityField in ability.fields) {
            when (field) {
                is AbilityField.TypeField ->
                    log("      Type: ${field.value.lexeme}")

                is AbilityField.CooldownField ->
                    log("      Cooldown: ${stringify(evalExpr(field.value, env))}")

                is AbilityField.ManaCostField ->
                    log("      Mana Cost: ${stringify(evalExpr(field.value, env))}")

                is AbilityField.RangeField ->
                    log("      Range: ${stringify(evalExpr(field.value, env))}")

                is AbilityField.DamageTypeField ->
                    log("      Damage Type: ${field.value.lexeme}")

                is AbilityField.BehaviorField -> {
                    log("      Behavior block:")
                    // Execute behavior in a fresh child environment of this hero/ability
                    val behaviorEnv = Environment(env)
                    if (field.body != null) {
                        executeBlock(field.body, behaviorEnv, indent = "        ")
                    } else if (field.expression != null) {
                        val result = evalExpr(field.expression, behaviorEnv)
                        log("        Pipeline Result: ${stringify(result)}")
                    }
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
                log("\n=== Team: ${item.name.lexeme} ===")
                environment.define(item.name, "Team")
                // Process Turrets inside the team
                for (turret in item.turrets) {
                    evaluateArenaItem(turret)
                }
            }
            is Decl.TurretDecl -> {
                log("\n=== Turret: ${item.name.lexeme} ===")
                for (stat in item.stats) {
                    val value = evalExpr(stat.value, environment)
                    log("  ${stat.name.lexeme}: ${stringify(value)}")
                }
            }
            is Decl.CoreDecl -> {
                log("\n=== Core: ${item.name.lexeme} ===")
                for (stat in item.stats) {
                    val value = evalExpr(stat.value, environment)
                    log("  ${stat.name.lexeme}: ${stringify(value)}")
                }
            }
            is Decl.ConstDecl -> {
                log("\n=== Const: ${item.name.lexeme} ===")
                val value = evalExpr(item.value, environment)
                environment.define(item.name, value)
                log("  Value: ${stringify(value)}")
            }
            is Decl.VarDecl -> {
                val value = evalExpr(item.value, environment)
                environment.define(item.name, value)
                log("  Set ${item.name.lexeme} = ${stringify(value)}")
            }
            is Decl.HeroDecl -> evaluateHero(item)
            is Decl.CreepDecl -> evaluateCreep(item)
            is Decl.ItemDecl -> evaluateItem(item)
            is Decl.StatusEffectDecl -> evaluateStatusEffect(item)
            is Decl.ImportDecl -> log("  Imported: ${item.name.lexeme}")
            is Decl.FunctionDecl -> log("\n=== Function Decl: ${item.name.lexeme} (...) ===")
        }
    }

    // ============================================================
    //                    STATUS EFFECTS
    // ============================================================
    private fun evaluateStatusEffect(effect: Decl.StatusEffectDecl) {
        log("\n=== Status Effect: ${effect.name.lexeme} ===")
        for (field: StatusEffectField in effect.fields) {
            when (field) {
                is StatusEffectField.TypeField ->
                    log("  Type: ${field.value.lexeme}")

                is StatusEffectField.DurationField ->
                    log("  Duration: ${stringify(evalExpr(field.value, environment))}")

                is StatusEffectField.OnApplyField -> {
                    log("  On Apply:")
                    val applyEnv = Environment(environment)
                    executeBlock(field.block, applyEnv, indent = "    ")
                }

                is StatusEffectField.OnTickField -> {
                    log("  On Tick:")
                    val tickEnv = Environment(environment)
                    executeBlock(field.block, tickEnv, indent = "    ")
                }

                is StatusEffectField.OnExpireField -> {
                    log("  On Expire:")
                    val expireEnv = Environment(environment)
                    executeBlock(field.block, expireEnv, indent = "    ")
                }
            }
        }
    }

    // ============================================================
    //                           ITEMS
    // ============================================================
    private fun evaluateItem(item: Decl.ItemDecl) {
        log("\n=== Item: ${item.name.lexeme} ===")
        for (field: ItemField in item.fields) {
            when (field) {
                is ItemField.PropertyField -> {
                    val value = evalExpr(field.value, environment)
                    log("  ${field.name.lexeme}: ${stringify(value)}")
                }
                is ItemField.PassiveField -> {
                    log("  Passive Effect:")
                    val effectEnv = Environment(environment)
                    val result = evalExpr(field.behavior, effectEnv)
                    log("    Pipeline Result: ${stringify(result)}")
                }
            }
        }
    }

    // ============================================================
    //                           CREEPS
    // ============================================================
    private fun evaluateCreep(creep: Decl.CreepDecl) {
        log("\n=== Creep: ${creep.name.lexeme} ===")
        for (stat in creep.stats) {
            val value = evalExpr(stat.value, environment)
            log("  ${stat.name.lexeme}: ${stringify(value)}")
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
                log("${indent}Apply $callStr to $targetStr")
            }

            is Stmt.SetStmt -> {
                val value = evalExpr(stmt.value, env)
                if (env.isDefinedAnywhere(stmt.name)) {
                    env.assign(stmt.name, value)
                } else {
                    env.define(stmt.name, value)
                }
                log("${indent}Set ${stmt.name.lexeme} = ${stringify(value)}")
            }

            is Stmt.ConstDeclStmt -> {
                val value = evalExpr(stmt.value, env)
                env.define(stmt.name, value)
                log("${indent}Const ${stmt.name.lexeme} = ${stringify(value)}")
            }

            is Stmt.StatEntryStmt -> {
                val value = evalExpr(stmt.entry.value, env)
                env.define(stmt.entry.name, value)
                log("${indent}Stat ${stmt.entry.name.lexeme} = ${stringify(value)}")
            }

            is Stmt.FunctionCallStmt -> {
                val result = evalExpr(Expr.FunctionCallExpr(stmt.call), env)
                if (stmt.call.name.lexeme != "printLog") {
                    log("${indent}Call ${stmt.call.name.lexeme}(...) -> ${stringify(result)}")
                }
            }

            is Stmt.ExprStmt -> {
                val result = evalExpr(stmt.expr, env)
                log("${indent}Expr ${exprToString(stmt.expr, env)} -> ${stringify(result)}")
            }

            is Stmt.IfStmt -> {
                val cond = evalExpr(stmt.condition, env)
                if (isTruthy(cond)) {
                    val child = Environment(env)
                    log("${indent}If ${exprToString(stmt.condition, env)} then:")
                    executeBlock(stmt.thenBranch, child, indent + "  ")
                } else if (stmt.elseBranch != null) {
                    val child = Environment(env)
                    log("${indent}Else:")
                    executeBlock(stmt.elseBranch, child, indent + "  ")
                }
            }

            is Stmt.WhileStmt -> {
                log("${indent}While loop:")
                // Simple simulation limit to prevent infinite loops in basic evaluation
                var limit = 5
                while (isTruthy(evalExpr(stmt.condition, env)) && limit-- > 0) {
                    val bodyEnv = Environment(env)
                    executeBlock(stmt.body, bodyEnv, indent + "  ")
                }
                if (limit == 0) log("${indent}  (Loop limit reached)")
            }

            is Stmt.ForStmt -> {
                log("${indent}For loop (${stmt.variable.lexeme}):")
                // Simplified for-loop simulation
                val collection = evalExpr(stmt.collection, env)
                log("${indent}  Iterating over: ${stringify(collection)}")
                // Real iteration logic would require collection support in Expr/Environment
            }

            is Stmt.ReturnStmt -> {
                val value = stmt.value?.let { evalExpr(it, env) }
                log("${indent}Return ${value?.let { stringify(it) } ?: "nil"}")
            }

            is Stmt.FunStmt -> {
                log("${indent}Function '${stmt.variable.lexeme}' declared")
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
            if (env.isDefinedAnywhere(expr.call.name)) {
                val value = env.get(expr.call.name)
                if (value is NativeFunction) {
                    val args = expr.call.arguments.map { arg ->
                        when (arg) {
                            is Argument.PositionalArg -> evalExpr(arg.value, env)
                            is Argument.NamedArg -> evalExpr(arg.value, env)
                        }
                    }
                    value.call(ExecutionContext(World()), args)
                } else {
                    functionCallToString(expr.call, env)
                }
            } else {
                functionCallToString(expr.call, env)
            }
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
                    throw RuntimeException("Operands must be two numbers or two strings. Got: ${stringify(left)} and ${stringify(right)}")
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
            TokenType.PIPE_GREATER -> "Pipeline($left -> $right)"

            else -> throw RuntimeException("Unsupported operator: ${operator.lexeme}")
        }
    }

    // ============================================================
    //                        HELPERS
    // ============================================================
    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        if (value is Boolean) return value
        if (value is Double) return value != 0.0
        return true
    }

    private fun expectNumber(operator: Token, value: Any?): Double {
        if (value is Double) return value
        if (value is Int) return value.toDouble()
        throw RuntimeException("Operand must be a number (at ${operator.lexeme}), got: $value")
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
        is Expr.Binary -> "(${exprToString(expr.left, env)} ${expr.operator.lexeme} ${exprToString(expr.right, env)})"
        is Expr.Grouping -> "(${exprToString(expr.expression, env)})"
        is Expr.FunctionCallExpr -> functionCallToString(expr.call, env)
        is Expr.Logical -> "(${exprToString(expr.left, env)} ${expr.operator.lexeme} ${exprToString(expr.right, env)})"
        is Expr.Unary -> "(${expr.operator.lexeme}${exprToString(expr.right, env)})"
    }
}