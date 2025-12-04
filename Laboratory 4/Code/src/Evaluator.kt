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
        // Imports
        for (importDecl in program.imports) {
            // Processing imports (no-op for now)
        }

        // Global variables
        for (varDecl in program.variables) {
            val value = evalExpr(varDecl.value, environment)
            environment.define(varDecl.name, value)
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

        // Functions (if present in Program AST, though not explicitly in previous definition)
        // If Program.kt has been updated to include 'functions', iteration would go here.
    }

    // ============================================================
    //                        HEROES
    // ============================================================
    private fun evaluateHero(hero: Decl.HeroDecl) {
        val heroEnv = Environment(environment)

        for (statement: HeroStatement in hero.statements) {
            when (statement) {
                is HeroStatement.SetStmt -> {
                    val value = evalExpr(statement.value, heroEnv)
                    heroEnv.define(statement.name, value)
                }
                is HeroStatement.HeroStatBlock -> {
                    for (stat in statement.stats) {
                        val value = evalExpr(stat.value, heroEnv)
                        heroEnv.define(stat.name, value)
                    }
                }
                is HeroStatement.AbilitiesBlock -> {
                    for (ability in statement.abilities) {
                        evaluateAbility(ability, heroEnv)
                    }
                }
                else -> {
                    // Handle potential new HeroStatement types
                }
            }
        }
    }

    private fun evaluateAbility(ability: AbilityDecl, env: Environment) {
        for (field: AbilityField in ability.fields) {
            when (field) {
                is AbilityField.TypeField -> {}
                is AbilityField.CooldownField -> { evalExpr(field.value, env) }
                is AbilityField.ManaCostField -> { evalExpr(field.value, env) }
                is AbilityField.RangeField -> { evalExpr(field.value, env) }
                is AbilityField.DamageTypeField -> {}
                is AbilityField.BehaviorField -> {
                    // Pre-calculate behavior environment if needed, or execute immediately for testing
                    val behaviorEnv = Environment(env)
                    // Check if body is nullable (based on grammar it might be pipeline_expr OR block)
                    // Assuming field.body is the BlockStmt? property
                    val body: BlockStmt? = field.body
                    if (body != null) {
                        executeBlock(body, behaviorEnv)
                    }
                }
                else -> {
                    // Handle potential new AbilityField types
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
                environment.define(item.name, "Team")
            }
            is Decl.TurretDecl -> {
                for (stat in item.stats) {
                    evalExpr(stat.value, environment)
                }
            }
            is Decl.CoreDecl -> {
                for (stat in item.stats) {
                    evalExpr(stat.value, environment)
                }
            }
            is Decl.ConstDecl -> {
                val value = evalExpr(item.value, environment)
                environment.define(item.name, value)
            }
            is Decl.VarDecl -> {
                val value = evalExpr(item.value, environment)
                environment.define(item.name, value)
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
            is Decl.ImportDecl -> {}
            is Decl.FunctionDecl -> {
                // Function definitions
            }
            else -> {
                // Handle potential new Decl types
            }
        }
    }

    // ============================================================
    //                    STATUS EFFECTS
    // ============================================================
    private fun evaluateStatusEffect(effect: Decl.StatusEffectDecl) {
        for (field: StatusEffectField in effect.fields) {
            when (field) {
                is StatusEffectField.TypeField -> {}
                is StatusEffectField.DurationField -> { evalExpr(field.value, environment) }
                is StatusEffectField.OnApplyField -> {
                    val applyEnv = Environment(environment)
                    executeBlock(field.block, applyEnv)
                }
                is StatusEffectField.OnTickField -> {
                    val tickEnv = Environment(environment)
                    executeBlock(field.block, tickEnv)
                }
                is StatusEffectField.OnExpireField -> {
                    val expireEnv = Environment(environment)
                    executeBlock(field.block, expireEnv)
                }
                else -> {
                    // Handles other potential fields
                }
            }
        }
    }

    // ============================================================
    //                           ITEMS
    // ============================================================
    private fun evaluateItem(item: Decl.ItemDecl) {
        for (field: ItemField in item.fields) {
            when (field) {
                is ItemField.PropertyField -> {
                    evalExpr(field.value, environment)
                }
                is ItemField.EffectField -> {
                    val effectEnv = Environment(environment)
                    val body: BlockStmt? = field.body
                    if (body != null) {
                        executeBlock(body, effectEnv)
                    }
                }
                is ItemField.PassiveField -> {
                    // Grammar: "passive" ":" "{" "behavior" ":" pipeline_expr "}"
                    // Evaluating the behavior expression
                    evalExpr(field.behavior, environment)
                }
                else -> {
                }
            }
        }
    }

    // ============================================================
    //                           CREEPS
    // ============================================================
    private fun evaluateCreep(creep: Decl.CreepDecl) {
        for (stat in creep.stats) {
            evalExpr(stat.value, environment)
        }
    }

    // ============================================================
    //                       STATEMENTS
    // ============================================================
    private fun executeBlock(block: BlockStmt, env: Environment) {
        for (stmt in block.statements) {
            execute(stmt, env)
        }
    }

    private fun execute(stmt: Stmt, env: Environment) {
        when (stmt) {
            is Stmt.ApplyStmt -> {
                functionCallToString(stmt.call, env)
                targetToString(stmt.target)
            }

            is Stmt.SetStmt -> {
                val value = evalExpr(stmt.value, env)
                if (env.isDefinedAnywhere(stmt.name)) {
                    env.assign(stmt.name, value)
                } else {
                    env.define(stmt.name, value)
                }
            }

            is Stmt.StatEntryStmt -> {
                val value = evalExpr(stmt.entry.value, env)
                env.define(stmt.entry.name, value)
            }

            is Stmt.FunctionCallStmt -> {
                if (stmt.call.name.lexeme == "printLog") {
                    printLog(env)
                } else {
                    evalExpr(Expr.FunctionCallExpr(stmt.call), env)
                }
            }

            is Stmt.ExprStmt -> {
                evalExpr(stmt.expr, env)
            }

            is Stmt.IfStmt -> {
                val cond = evalExpr(stmt.condition, env)
                if (isTruthy(cond)) {
                    val child = Environment(env)
                    executeBlock(stmt.thenBranch, child)
                } else if (stmt.elseBranch != null) {
                    val child = Environment(env)
                    val elseBlock = stmt.elseBranch
                    if (elseBlock != null) {
                        executeBlock(elseBlock, child)
                    } else if (stmt is Stmt.IfStmt && stmt.elseBranch == null) {
                        // Handle potential else-if structure if represented differently
                    }
                }
            }

            is Stmt.WhileStmt -> {
                val condExpr = stmt.condition
                while (true) {
                    if (condExpr != null && !isTruthy(evalExpr(condExpr, env))) {
                        break
                    }
                    val bodyEnv = Environment(env)
                    executeBlock(stmt.body, bodyEnv)
                    if (condExpr == null) break // Safety break if condition missing
                }
            }

            is Stmt.ForStmt -> {
                // Grammar: for (IDENT in expression)
                // Assuming AST has properties: variable (Token) and iterable (Expr)
                val loopEnv = Environment(env)

                val iterableValue = evalExpr(stmt.iterable, env)

                // Placeholder iteration logic
                // Without a Collection/List type in the Evaluator, we cannot fully implement iteration.
                // If iterableValue is a number N, we could loop N times.

                if (iterableValue is Double) {
                    val count = iterableValue.toInt()
                    for (i in 0 until count) {
                        loopEnv.define(stmt.variable, i)
                        executeBlock(stmt.body, loopEnv)
                    }
                } else {
                    // Just execute once for now or log warning
                    // println("Warning: Iteration over non-number ${stringify(iterableValue)} not fully supported.")
                    loopEnv.define(stmt.variable, iterableValue)
                    executeBlock(stmt.body, loopEnv)
                }
            }

            is Stmt.ReturnStmt -> {
                stmt.value?.let { evalExpr(it, env) }
            }

            is Stmt.FunStmt -> {
                // Function definition logic
            }

            is Stmt.ConstDeclStmt -> {
                // Grammar: const type IDENT = expression ;
                val value = evalExpr(stmt.value, env)
                env.define(stmt.name, value)
            }

            is Stmt.AssignmentStmt -> {
                // Grammar: IDENT = expression ; (Reassignment)
                val value = evalExpr(stmt.value, env)
                env.assign(stmt.name, value)
            }

            else -> {
                // Handles other potential statements
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
            if (expr.call.name.lexeme == "printLog") {
                printLog(env)
                "Log Output"
            } else {
                functionCallToString(expr.call, env)
            }
        }

        is Expr.Logical -> evalLogical(expr, env)

        is Expr.Unary -> evalUnary(expr, env)
    }

    // ============================================================
    //                     BUILT-IN FUNCTIONS
    // ============================================================

    private fun printLog(env: Environment) {
        println("\n=== EXECUTION LOG ===")
        var current: Environment? = env
        var scopeLevel = 0

        while (current != null) {
            val vars = current.getLocalVariables()
            if (vars.isNotEmpty()) {
                val scopeName = if (scopeLevel == 0) "Current Scope" else "Enclosing Scope $scopeLevel"
                println("--- $scopeName ---")
                for ((k, v) in vars) {
                    println("$k: ${stringify(v)}")
                }
            }
            current = current.enclosing
            scopeLevel++
        }
        println("=====================\n")
    }

    // ============================================================
    //                        HELPERS
    // ============================================================

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

            // Pipeline Operator |>
            TokenType.PIPE_GREATER -> {
                // Grammar: function_call { "|>" function_call }
                // Implementation: Just stringifying the pipeline operation for now
                "${stringify(left)} |> ${stringify(right)}"
            }

            else -> throw RuntimeException("Unsupported operator: ${operator.lexeme}")
        }
    }

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