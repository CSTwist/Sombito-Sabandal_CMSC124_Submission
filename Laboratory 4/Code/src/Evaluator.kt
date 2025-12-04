// Evaluator.kt
import kotlin.math.floor

class Evaluator {
    private var environment = Environment()

    fun evaluateProgram(program: Program) {
        println("=== Program: ${program.name.lexeme} ===")
        // 1. Imports
        program.imports.forEach { println("Import: ${it.name.lexeme}") }

        // 2. Global Vars
        program.variables.forEach {
            val v = evalExpr(it.value, environment)
            environment.define(it.name, v)
            println("Global ${it.name.lexeme} = $v")
        }

        // 3. Functions (Just define them, don't run)
        program.functions.forEach { println("Defined Function: ${it.name.lexeme}") }

        // 4. Teams
        program.teams.forEach { team ->
            println("Team: ${team.name.lexeme}")
            team.turrets.forEach { t -> println("  Turret: ${t.name.lexeme}") }
        }

        // 5. Items (Passive Pipeline)
        program.items.forEach { item ->
            println("Item: ${item.name.lexeme}")
            item.fields.forEach {
                if (it is ItemField.PassiveField) {
                    println("  Passive Pipeline: ${it.behavior}")
                }
            }
        }
    }

    fun evaluate(expr: Expr): Any? {
        return evalExpr(expr, environment)
    }

    private fun execute(stmt: Stmt, env: Environment) {
        when (stmt) {
            is Stmt.WhileStmt -> {
                while (isTruthy(evalExpr(stmt.condition, env))) {
                    executeBlock(stmt.body, Environment(env))
                }
            }
            is Stmt.ForStmt -> {
                println("For loop not fully impl (needs collection iterator)")
            }
            is Stmt.ConstDeclStmt -> {
                val v = evalExpr(stmt.value, env)
                env.define(stmt.name, v)
            }
            is Stmt.SetStmt -> {
                if (env.isDefinedAnywhere(stmt.name)) env.assign(stmt.name, evalExpr(stmt.value, env))
                else env.define(stmt.name, evalExpr(stmt.value, env))
            }
            else -> println("Stmt execution...")
        }
    }

    private fun executeBlock(block: BlockStmt, env: Environment) {
        block.statements.forEach { execute(it, env) }
    }

    private fun evalExpr(expr: Expr, env: Environment): Any? = when (expr) {
        is Expr.Binary -> {
            val left = evalExpr(expr.left, env)
            val right = evalExpr(expr.right, env)
            if (expr.operator.type == TokenType.PIPE_GREATER) {
                println("Executing Pipeline: $left |> $right")
                "PipelineResult"
            } else {
                0.0 // Placeholder for math
            }
        }
        is Expr.Literal -> expr.value
        is Expr.Variable -> env.get(expr.name)
        else -> null
    }

    private fun isTruthy(v: Any?) = v != null && v != false
}