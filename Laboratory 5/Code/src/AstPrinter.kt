// AstPrinter.kt
import kotlin.math.floor

class AstPrinter {

    fun print(program: Program) {
        println(astToString(program))
    }

    private fun astToString(program: Program): String = buildString {
        appendLine("(Program: ${program.name.lexeme}")

        if (program.imports.isNotEmpty()) {
            appendLine("  (Imports")
            program.imports.forEach { appendLine("    (Import ${it.name.lexeme})") }
            appendLine("  )")
        }

        if (program.functions.isNotEmpty()) {
            appendLine("  (Functions")
            program.functions.forEach { func ->
                append("    (Function ${func.name.lexeme} (")
                func.params.forEach { append("${it.name.lexeme}:${it.type.lexeme} ") }
                appendLine(") ${blockToString(func.body)})")
            }
            appendLine("  )")
        }

        if (program.teams.isNotEmpty()) {
            appendLine("  (Teams")
            program.teams.forEach { team ->
                appendLine("    (Team ${team.name.lexeme}")
                if (team.coreRef != null) appendLine("      (CoreRef ${team.coreRef.lexeme})")
                team.turrets.forEach { t ->
                    appendLine("      (Turret ${t.name.lexeme} ...)")
                }
                appendLine("    )")
            }
            appendLine("  )")
        }

        // ... (Remaining sections can follow similar logic) ...
        // Reusing existing printers mostly
        appendLine("  (Heroes ...)")
        program.heroes.forEach { appendLine(indent(heroToString(it), 4)) }
        append(")")
    }

    private fun heroToString(hero: Decl.HeroDecl): String = buildString {
        appendLine("(Hero ${hero.name.lexeme}")
        hero.statements.forEach { appendLine(indent(heroStatementToString(it), 2)) }
        append(")")
    }

    private fun heroStatementToString(stmt: HeroStatement): String = when (stmt) {
        is HeroStatement.SetStmt -> "(Set ${stmt.name.lexeme} = ${exprToString(stmt.value)})"
        is HeroStatement.HeroStatBlock -> "(HeroStats ...)"
        is HeroStatement.AbilitiesBlock -> "(Abilities ...)"
    }

    // Updated Block Printer for Loops
    private fun stmtToString(stmt: Stmt): String = when (stmt) {
        is Stmt.WhileStmt -> "(While ${exprToString(stmt.condition)} ${blockToString(stmt.body)})"
        is Stmt.ForStmt -> "(For ${stmt.variable.lexeme} in ${exprToString(stmt.collection)} ${blockToString(stmt.body)})"
        is Stmt.ReturnStmt -> "(Return ...)"
        is Stmt.ConstDeclStmt -> "(Const ${stmt.name.lexeme} ...)"
        is Stmt.SetStmt -> "(Set ${stmt.name.lexeme} ...)"
        is Stmt.ApplyStmt -> "(Apply ${stmt.call.name.lexeme} ...)"
        is Stmt.StatEntryStmt -> "(Stat ${stmt.entry.name.lexeme} ...)"
        is Stmt.ExprStmt -> "(Expr ...)"
        is Stmt.IfStmt -> "(If ...)"
        is Stmt.FunctionCallStmt -> "(Call ...)"
        is Stmt.FunStmt -> TODO()
    }

    private fun blockToString(block: BlockStmt): String = "{ " + block.statements.joinToString(" ") { stmtToString(it) } + " }"

    private fun exprToString(expr: Expr): String = when (expr) {
        is Expr.Binary -> "(${expr.operator.lexeme} ${exprToString(expr.left)} ${exprToString(expr.right)})"
        is Expr.FunctionCallExpr -> "${expr.call.name.lexeme}(...)"
        is Expr.Literal -> "${expr.value}"
        is Expr.Variable -> expr.name.lexeme
        else -> "..."
    }

    private fun indent(text: String, spaces: Int): String {
        val prefix = " ".repeat(spaces)
        return text.lines().joinToString("\n") { prefix + it }
    }
}