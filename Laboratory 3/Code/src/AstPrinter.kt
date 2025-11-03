import kotlin.math.floor

// AstPrinter.kt
class AstPrinter {

    fun print(program: Program) {
        println(astToString(program))
    }

    private fun astToString(program: Program): String =
        program.decls.joinToString("\n") { declToString(it) }

    // ---- DECLARATIONS ----
    private fun declToString(decl: Decl): String = when (decl) {
        is Decl.FunDecl -> buildString {
            append("(FunDecl ")
            decl.returnType?.let { append("(Type ${it.lexeme}) ") }
            append("(Name ${decl.name.lexeme}) ")

            if (decl.params.isNotEmpty()) {
                append("(Params ${decl.params.joinToString(" ") { it.lexeme }}) ")
            }

            append("(Body ${stmtToString(decl.body)})")

            decl.otherBlock?.let {
                append(" (Other ${stmtToString(it)})")
            }
            append(")")
        }

        is Decl.VarDecl ->
            "(VarDecl ${decl.name.lexeme}${decl.initializer?.let { " = ${exprToString(it)}" } ?: ""})"

        is Decl.ConstDecl ->
            "(ConstDecl ${decl.name.lexeme}${decl.value?.let { " = ${exprToString(it)}" } ?: ""})"

        is Decl.ArayDecl -> buildString {
            append("(ArayDecl ${decl.name.lexeme} { ")
            append(decl.members.joinToString(" ") { arayMemberToString(it) })
            append(" })")
        }

        is Decl.EnumDecl ->
            "(EnumDecl ${decl.name.lexeme} { ${decl.entries.joinToString(" ") { it.lexeme }} })"
    }

    private fun arayMemberToString(m: Decl.ArayMember): String =
        "(Member ${m.type.lexeme} ${m.name.lexeme}${m.size?.let { " [$it]" } ?: ""})"

    // ---- STATEMENTS ----
    private fun stmtToString(stmt: Stmt): String = when (stmt) {
        is Stmt.ExpressionStmt ->
            "(ExprStmt ${exprToString(stmt.expression)})"

        is Stmt.PrintStmt ->
            "(PrintStmt ${exprToString(stmt.expression)})"

        is Stmt.VarDecl ->
            "(VarDecl ${stmt.name.lexeme}${stmt.initializer?.let { " = ${exprToString(it)}" } ?: ""})"

        is Stmt.Block ->
            "(Block ${stmt.statements.joinToString(" ") { stmtToString(it) }})"

        is Stmt.IfStmt -> buildString {
            append("(IfStmt ${exprToString(stmt.condition)} ")
            append(stmtToString(stmt.thenBranch))
            for ((cond, branch) in stmt.elseIfBranches) {
                append(" (ElseIf ${exprToString(cond)} ${stmtToString(branch)})")
            }
            stmt.elseBranch?.let { append(" (Else ${stmtToString(it)})") }
            append(")")
        }

        is Stmt.WhileStmt ->
            "(WhileStmt ${exprToString(stmt.condition)} ${stmtToString(stmt.body)})"

        is Stmt.ForStmt -> buildString {
            append("(ForStmt ")
            stmt.initializer?.let { append(stmtToString(it) + " ") }
            stmt.condition?.let { append(exprToString(it) + " ") }
            stmt.increment?.let { append(exprToString(it) + " ") }
            append(stmtToString(stmt.body))
            append(")")
        }

        is Stmt.ReturnStmt ->
            "(ReturnStmt ${stmt.value?.let { exprToString(it) } ?: "nil"})"

        else -> "(UnknownStmt)"
    }

    // ---- EXPRESSIONS ----
    private fun exprToString(expr: Expr): String = when (expr) {
        is Expr.Literal -> literalToString(expr.value)
        is Expr.Grouping -> "(group ${exprToString(expr.expression)})"
        is Expr.Unary -> "(${expr.operator.lexeme} ${exprToString(expr.right)})"
        is Expr.Binary -> "(${expr.operator.lexeme} ${exprToString(expr.left)} ${exprToString(expr.right)})"
        is Expr.Variable -> "(var ${expr.name.lexeme})"
    }

    private fun literalToString(v: Any?): String =
        when (v) {
            null -> "nil"
            is String -> "\"$v\""
            is Double -> if (v == floor(v)) "${v.toInt()}" else v.toString()
            else -> v.toString()
        }
}