// Stmt.kt
//
// All executable statement types for the DSL (not declarations!)
// Used by Parser, Evaluator, and AstPrinter.
//

sealed interface Stmt {

    // --------------------------
    // Simple statements
    // --------------------------
    data class ExprStmt(val expr: Expr) : Stmt
    data class SetStmt(val name: Token, val value: Expr) : Stmt
    data class ConstDeclStmt(val type: Token, val name: Token, val value: Expr) : Stmt

    // hero.hp = 100  (inside hero declarations)
    data class StatEntryStmt(val entry: StatEntry) : Stmt

    // abilityCall(...)
    data class FunctionCallStmt(val call: FunctionCall) : Stmt

    // apply strike() to enemy;
    data class ApplyStmt(val call: FunctionCall, val target: TargetExpr) : Stmt

    // --------------------------
    // Control flow
    // --------------------------
    data class IfStmt(
        val condition: Expr,
        val thenBranch: BlockStmt,
        val elseBranch: BlockStmt?
    ) : Stmt

    data class WhileStmt(
        val condition: Expr,
        val body: BlockStmt
    ) : Stmt

    data class ForStmt(
        val variable: Token,
        val collection: Expr,
        val body: BlockStmt
    ) : Stmt

    data class ReturnStmt(
        val keyword: Token,
        val value: Expr?
    ) : Stmt

    // function foo … (Used by parser during body parsing)
    data class FunStmt(val variable: Token) : Stmt
}

// A block is simply a list of Stmts
data class BlockStmt(val statements: List<Stmt>)
