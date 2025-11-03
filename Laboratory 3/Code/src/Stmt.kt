sealed interface Stmt {
    data class ExpressionStmt(val expression: Expr) : Stmt
    data class PrintStmt(val expression: Expr) : Stmt
    data class VarDecl(val name: Token, val initializer: Expr?) : Stmt
    data class Block(val statements: List<Stmt>) : Stmt
    data class IfStmt(
        val condition: Expr,
        val thenBranch: Stmt,
        val elseIfBranches: List<Pair<Expr, Stmt>>,
        val elseBranch: Stmt?
    ) : Stmt
    data class WhileStmt(val condition: Expr, val body: Stmt) : Stmt
    data class ForStmt(
        val initializer: Stmt?,
        val condition: Expr?,
        val increment: Expr?,
        val body: Stmt
    ) : Stmt
    data class ReturnStmt(val keyword: Token, val value: Expr?) : Stmt
}