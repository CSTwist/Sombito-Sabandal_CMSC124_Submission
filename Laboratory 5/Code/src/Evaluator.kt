// Evaluator.kt
//
// Evaluates the AST produced by the parser.
// Supports hero evaluation, ability behavior execution,
// variable evaluation, pipelines, and exporting text files.
//

import java.io.File

class Evaluator {

    private val globals = Environment()
    private var environment = globals

    // ============================================================
    // ENTRY POINT
    // ============================================================
    fun evaluate(program: Program) {
        // 1. Load top-level variables
        program.variables.forEach { evalVarDecl(it) }

        // 2. Load heroes (as maps) into environment
        program.heroes.forEach { evalHeroDecl(it) }

        // 3. Load items / creeps / status effects into environment
        program.items.forEach { evalItemDecl(it) }
        program.creeps.forEach { evalCreepDecl(it) }
        program.statusEffects.forEach { evalStatusEffectDecl(it) }

        // 4. Load functions
        program.functions.forEach { globals.define(it.name.lexeme, it) }

        // Optional debugging:
        // environment.debugDump("After Program Loaded")
    }

    // ============================================================
    // VARIABLE DECLARATIONS
    // ============================================================
    private fun evalVarDecl(decl: Decl.VarDecl) {
        val value = evalExpr(decl.value)
        environment.define(decl.name.lexeme, value)
    }

    // ============================================================
    // HERO DECL
    // ============================================================
    private fun evalHeroDecl(decl: Decl.HeroDecl) {
        val heroMap = mutableMapOf<String, Any?>()

        decl.statements.forEach { stmt ->
            when (stmt) {
                is HeroStatement.SetStmt -> {
                    heroMap[stmt.name.lexeme] = evalExpr(stmt.value)
                }
                is HeroStatement.HeroStatBlock -> {
                    stmt.stats.forEach { stat ->
                        heroMap[stat.name.lexeme] = evalExpr(stat.value)
                    }
                }
                is HeroStatement.AbilitiesBlock -> {
                    heroMap["abilities"] = stmt.abilities.map { evalAbilityDecl(it) }
                }
            }
        }

        // Store hero object in environment
        environment.define(decl.name.lexeme, heroMap)
    }

    // ============================================================
    // ABILITY DECL
    // ============================================================
    private fun evalAbilityDecl(decl: AbilityDecl): Map<String, Any?> {
        val data = mutableMapOf<String, Any?>()

        decl.fields.forEach { field ->
            when (field) {
                is AbilityField.TypeField ->
                    data["type"] = field.value.lexeme

                is AbilityField.CooldownField ->
                    data["cooldown"] = evalExpr(field.value)

                is AbilityField.ManaCostField ->
                    data["mana_cost"] = evalExpr(field.value)

                is AbilityField.RangeField ->
                    data["range"] = evalExpr(field.value)

                is AbilityField.DamageTypeField ->
                    data["damage_type"] = field.value.lexeme

                is AbilityField.BehaviorField -> {
                    if (field.body != null) {
                        data["behavior"] = evalBlock(field.body)
                    } else if (field.expression != null) {
                        data["behavior"] = evalExpr(field.expression)
                    }
                }
            }
        }

        return data
    }

    // ============================================================
    // ITEMS
    // ============================================================
    private fun evalItemDecl(decl: Decl.ItemDecl) {
        val map = mutableMapOf<String, Any?>()
        decl.fields.forEach { field ->
            when (field) {
                is ItemField.PropertyField ->
                    map[field.name.lexeme] = evalExpr(field.value)

                is ItemField.PassiveField ->
                    map["passive"] = evalExpr(field.behavior)
            }
        }
        environment.define(decl.name.lexeme, map)
    }

    // ============================================================
    // CREEPS
    // ============================================================
    private fun evalCreepDecl(decl: Decl.CreepDecl) {
        val map = mutableMapOf<String, Any?>()
        decl.stats.forEach { map[it.name.lexeme] = evalExpr(it.value) }
        environment.define(decl.name.lexeme, map)
    }

    // ============================================================
    // STATUS EFFECTS
    // ============================================================
    private fun evalStatusEffectDecl(decl: Decl.StatusEffectDecl) {
        val map = mutableMapOf<String, Any?>()
        decl.fields.forEach { f ->
            when (f) {
                is StatusEffectField.TypeField ->
                    map["type"] = f.value.lexeme

                is StatusEffectField.DurationField ->
                    map["duration"] = evalExpr(f.value)

                is StatusEffectField.OnApplyField ->
                    map["on_apply"] = evalBlock(f.block)

                is StatusEffectField.OnTickField ->
                    map["on_tick"] = evalBlock(f.block)

                is StatusEffectField.OnExpireField ->
                    map["on_expire"] = evalBlock(f.block)
            }
        }
        environment.define(decl.name.lexeme, map)
    }

    // ============================================================
    // BLOCK STATEMENTS
    // ============================================================
    private fun evalBlock(block: BlockStmt): Any? {
        val previous = environment
        environment = environment.createChild()

        var result: Any? = null

        try {
            for (stmt in block.statements) {
                val value = evalStmt(stmt)
                if (stmt is Stmt.ReturnStmt) {
                    return value
                }
                result = value
            }
        } finally {
            environment = previous
        }

        return result
    }

    // ============================================================
    // STATEMENTS
    // ============================================================
    private fun evalStmt(stmt: Stmt): Any? {
        return when (stmt) {

            is Stmt.SetStmt -> {
                val value = evalExpr(stmt.value)
                environment.assign(stmt.name, value)
                value
            }

            is Stmt.ConstDeclStmt -> {
                val value = evalExpr(stmt.value)
                environment.define(stmt.name.lexeme, value)
                value
            }

            is Stmt.ExprStmt -> evalExpr(stmt.expr)

            is Stmt.StatEntryStmt -> {
                evalExpr(stmt.entry.value)
            }

            is Stmt.ApplyStmt -> {
                val call = evalFunctionCall(stmt.call)
                call // For now we return the call result
            }

            is Stmt.WhileStmt -> {
                while (isTruthy(evalExpr(stmt.condition))) {
                    evalBlock(stmt.body)
                }
                null
            }

            is Stmt.IfStmt -> {
                if (isTruthy(evalExpr(stmt.condition))) {
                    evalBlock(stmt.thenBranch)
                } else stmt.elseBranch?.let { evalBlock(it) }
            }

            is Stmt.ForStmt -> {
                val list = evalExpr(stmt.collection)
                if (list !is Iterable<*>) return null
                for (item in list) {
                    val child = environment.createChild()
                    child.define(stmt.variable.lexeme, item)
                    val prev = environment
                    environment = child
                    evalBlock(stmt.body)
                    environment = prev
                }
                null
            }

            is Stmt.ReturnStmt -> stmt.value?.let { evalExpr(it) }

            else -> null
        }
    }

    // ============================================================
    // FUNCTION CALLS & PIPELINES
    // ============================================================
    private fun evalFunctionCall(call: FunctionCall): Any? {
        val fn = environment.get(call.name)

        // If DSL-level function
        if (fn is Decl.FunctionDecl) {
            val child = Environment(environment)

            // Bind parameters
            for ((i, param) in fn.params.withIndex()) {
                val arg = call.arguments.getOrNull(i)
                if (arg is Argument.PositionalArg) {
                    child.define(param.name.lexeme, evalExpr(arg.value))
                }
                if (arg is Argument.NamedArg) {
                    child.define(arg.name.lexeme, evalExpr(arg.value))
                }
            }

            val previous = environment
            environment = child
            val result = evalBlock(fn.body)
            environment = previous
            return result
        }

        // Built-in function example: print()
        if (call.name.lexeme == "print") {
            call.arguments.forEach { arg ->
                when (arg) {
                    is Argument.NamedArg -> println(evalExpr(arg.value))
                    is Argument.PositionalArg -> println(evalExpr(arg.value))
                }
            }
            return null
        }

        // Built-in: export("file.txt", value)
        if (call.name.lexeme == "export") {
            if (call.arguments.size == 2) {
                val fileName = evalExpr((call.arguments[0] as Argument.PositionalArg).value).toString()
                val content = evalExpr((call.arguments[1] as Argument.PositionalArg).value).toString()
                File(fileName).writeText(content)
            }
            return null
        }

        return null
    }

    // ============================================================
    // EXPRESSION EVALUATION
    // ============================================================
    private fun evalExpr(expr: Expr): Any? {
        return when (expr) {

            is Expr.Literal -> expr.value

            is Expr.Variable -> environment.get(expr.name)

            is Expr.Grouping -> evalExpr(expr.expression)

            is Expr.Unary -> {
                val right = evalExpr(expr.right)
                when (expr.operator.type) {
                    TokenType.MINUS -> -(right as Number).toDouble()
                    TokenType.BANG -> !isTruthy(right)
                    else -> null
                }
            }

            is Expr.Binary -> {
                val left = evalExpr(expr.left)
                val right = evalExpr(expr.right)

                when (expr.operator.type) {
                    TokenType.PLUS -> (left as Number).toDouble() + (right as Number).toDouble()
                    TokenType.MINUS -> (left as Number).toDouble() - (right as Number).toDouble()
                    TokenType.STAR -> (left as Number).toDouble() * (right as Number).toDouble()
                    TokenType.DIVIDE -> (left as Number).toDouble() / (right as Number).toDouble()
                    TokenType.EQUAL_EQUAL -> left == right
                    TokenType.BANG_EQUAL -> left != right
                    TokenType.PIPE_GREATER -> applyPipeline(left, right)
                    else -> null
                }
            }

            is Expr.Logical -> {
                val left = evalExpr(expr.left)
                if (expr.operator.type == TokenType.OR) {
                    if (isTruthy(left)) return left
                } else {
                    if (!isTruthy(left)) return left
                }
                evalExpr(expr.right)
            }

            is Expr.Percentage -> expr.value / 100.0

            is Expr.Time -> expr.seconds

            is Expr.FunctionCallExpr -> evalFunctionCall(expr.call)
        }
    }

    // ============================================================
    // PIPELINE SUPPORT
    // ============================================================
    private fun applyPipeline(left: Any?, right: Any?): Any? {
        // No-op for now until you define custom pipeline logic
        return right
    }

    // ============================================================
    // UTILITIES
    // ============================================================
    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        if (value is Boolean) return value
        return true
    }
}
