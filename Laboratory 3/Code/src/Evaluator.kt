// Evaluator.kt
import kotlin.math.floor

class Evaluator {

    private var environment = Environment()

    fun eval(program: Program) {
        for (decl in program.decls) evalDecl(decl)
    }

    /* ---------------- Declarations ---------------- */

    private fun evalDecl(d: Decl) {
        when (d) {
            is Decl.VarDecl -> {
                val value = d.initializer?.let { evalExpr(it) }
                environment.define(d.name, value, isConst = false)
            }
            is Decl.ConstDecl -> {
                val value = d.value?.let { evalExpr(it) }
                environment.define(d.name, value, isConst = true)
            }
            is Decl.FunDecl -> {
                // Minimal stub so functions can be stored (optional for this lab)
                val func = UserFunction(d, environment)
                environment.define(d.name, func, isConst = true)
            }
            is Decl.ArayDecl -> {
                // Simple runtime representation: map of member name -> (typeName, size)
                val meta = d.members.associate { it.name.lexeme to Pair(it.type.lexeme, it.size) }
                environment.define(d.name, meta, isConst = true)
            }
            is Decl.EnumDecl -> {
                val entries = d.entries.mapIndexed { idx, t -> t.lexeme to idx }.toMap()
                environment.define(d.name, entries, isConst = true)
            }
        }
    }

    /* ---------------- Statements ---------------- */

    private fun evalStmt(s: Stmt) {
        when (s) {
            is Stmt.ExpressionStmt -> {
                val v = evalExpr(s.expression)
                // REPL-like echo for bare expressions (convenient for the lab)
                println(stringify(v))
            }
            is Stmt.PrintStmt -> {
                val v = evalExpr(s.expression)
                println(stringify(v))
            }
            is Stmt.VarDecl -> {
                val value = s.initializer?.let { evalExpr(it) }
                environment.define(s.name, value, isConst = false)
            }
            is Stmt.Block -> executeBlock(s.statements, Environment(environment))
            is Stmt.IfStmt -> {
                if (isTruthy(evalExpr(s.condition))) {
                    evalStmt(s.thenBranch)
                } else {
                    var done = false
                    for ((cond, branch) in s.elseIfBranches) {
                        if (isTruthy(evalExpr(cond))) {
                            evalStmt(branch)
                            done = true
                            break
                        }
                    }
                    if (!done) s.elseBranch?.let { evalStmt(it) }
                }
            }
            is Stmt.WhileStmt -> {
                while (isTruthy(evalExpr(s.condition))) {
                    evalStmt(s.body)
                }
            }
            is Stmt.ForStmt -> {
                // initializer
                s.initializer?.let { evalStmt(it) }
                // condition defaults to true if absent
                while (s.condition?.let { isTruthy(evalExpr(it)) } ?: true) {
                    evalStmt(s.body)
                    s.increment?.let { evalExpr(it) }
                }
            }
            is Stmt.ReturnStmt -> {
                val v = s.value?.let { evalExpr(it) }
                throw Return(v)
            }
            else -> { /* no-op for unhandled cases */ }
        }
    }

    fun executeBlock(statements: List<Stmt>, newEnv: Environment) {
        val previous = environment
        try {
            environment = newEnv
            for (st in statements) evalStmt(st)
        } finally {
            environment = previous
        }
    }

    /* ---------------- Expressions ---------------- */

    public fun evalExpr(e: Expr): Any? = when (e) {
        is Expr.Literal -> e.value
        is Expr.Grouping -> evalExpr(e.expression)
        is Expr.Variable -> environment.get(e.name)

        is Expr.Unary -> {
            val right = evalExpr(e.right)
            when (e.operator.type) {
                TokenType.MINUS -> {
                    val num = expectNumber(e.operator, right)
                    -num
                }
                TokenType.BANG, TokenType.NOT_WORD -> !isTruthy(right)
                else -> RuntimeError.report(e.operator, "Invalid unary operator '${e.operator.lexeme}'.")
            }
        }

        is Expr.Binary -> {
            when (e.operator.type) {
                TokenType.EQUAL -> {
                    val value = evalExpr(e.right)
                    val left = e.left
                    if (left is Expr.Variable) {
                        environment.assign(left.name, value)
                        value
                    } else {
                        RuntimeError.report(e.operator, "Invalid assignment target.")
                    }
                }
                TokenType.AND_AND, TokenType.AND_WORD -> {
                    val left = evalExpr(e.left)
                    if (!isTruthy(left)) left else evalExpr(e.right)
                }
                TokenType.OR_OR, TokenType.OR_WORD -> {
                    val left = evalExpr(e.left)
                    if (isTruthy(left)) left else evalExpr(e.right)
                }
                else -> evalBinaryNonAssign(e)
            }
        }
    }

    private fun evalBinaryNonAssign(e: Expr.Binary): Any? {
        val left = evalExpr(e.left)
        val right = evalExpr(e.right)
        return when (e.operator.type) {
            // Arithmetic
            TokenType.PLUS -> {
                if (left is Double && right is Double) left + right
                else if (left is String && right is String) left + right
                else RuntimeError.report(e.operator, "Operands must be two numbers or two strings.")
            }
            TokenType.MINUS -> {
                val a = expectNumber(e.operator, left)
                val b = expectNumber(e.operator, right)
                a - b
            }
            TokenType.STAR -> {
                val a = expectNumber(e.operator, left)
                val b = expectNumber(e.operator, right)
                a * b
            }
            TokenType.DIVIDE -> {
                val a = expectNumber(e.operator, left)
                val b = expectNumber(e.operator, right)
                if (b == 0.0) RuntimeError.report(e.operator, "Division by zero.")
                a / b
            }

            // Comparisons (numbers only)
            TokenType.LESS -> {
                val a = expectNumber(e.operator, left)
                val b = expectNumber(e.operator, right)
                a < b
            }
            TokenType.LESS_EQUAL -> {
                val a = expectNumber(e.operator, left)
                val b = expectNumber(e.operator, right)
                a <= b
            }
            TokenType.GREATER -> {
                val a = expectNumber(e.operator, left)
                val b = expectNumber(e.operator, right)
                a > b
            }
            TokenType.GREATER_EQUAL -> {
                val a = expectNumber(e.operator, left)
                val b = expectNumber(e.operator, right)
                a >= b
            }

            // Equality (any types)
            TokenType.EQUAL_EQUAL -> equalsLox(left, right)
            TokenType.BANG_EQUAL -> !equalsLox(left, right)

            else -> RuntimeError.report(e.operator, "Unsupported operator '${e.operator.lexeme}'.")
        }
    }

    /* ---------------- Helpers ---------------- */

    private fun expectNumber(op: Token, v: Any?): Double {
        if (v is Double) return v
        RuntimeError.report(op, "Operand must be a number.")
    }

    private fun equalsLox(a: Any?, b: Any?): Boolean = a == b

    private fun isTruthy(v: Any?): Boolean = when (v) {
        null -> false
        is Boolean -> v
        else -> true
    }

    private fun stringify(v: Any?): String = when (v) {
        null -> "nil"
        is String -> v
        is Double -> if (v == floor(v)) v.toInt().toString() else v.toString()
        else -> v.toString()
    }

    /* ---------------- Minimal function object (optional) ---------------- */

    private class UserFunction(
        private val decl: Decl.FunDecl,
        private val closure: Environment
    ) {
        fun call(evaluator: Evaluator, args: List<Any?>): Any? {
            val env = Environment(closure)
            decl.params.forEachIndexed { i, p ->
                env.define(p, if (i < args.size) args[i] else null, isConst = false)
            }
            try {
                evaluator.executeBlock(decl.body.statements, env)
            } catch (r: Return) {
                return r.value
            }
            return null
        }

        override fun toString(): String = "<fun ${decl.name.lexeme}>"
    }
}
