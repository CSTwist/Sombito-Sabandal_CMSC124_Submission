// Expr.kt
//
// All expression types for the DSL.
// Updated to fully match the Parser, Evaluator, and AstPrinter.
// No redesign — only corrections and consistency improvements.
//

sealed class Expr {

    // -----------------------------
    // LITERALS
    // -----------------------------
    data class Literal(val value: Any?) : Expr()

    // Example: 50%  → stored as Expr.Percentage(50.0)
    data class Percentage(val value: Double) : Expr()

    // Example: 5s → Expr.Time(5)
    data class Time(val seconds: Int) : Expr()

    // -----------------------------
    // VARIABLES
    // -----------------------------
    data class Variable(val name: Token) : Expr()

    // -----------------------------
    // GROUPING
    // -----------------------------
    data class Grouping(val expression: Expr) : Expr()

    // -----------------------------
    // UNARY
    // -----------------------------
    data class Unary(val operator: Token, val right: Expr) : Expr()

    // -----------------------------
    // BINARY
    // Includes: + - * / comparisons == !=
    // Also supports pipeline operator |>
    // -----------------------------
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    // -----------------------------
    // LOGICAL EXPRESSIONS
    // For AND / OR
    // -----------------------------
    data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()

    // -----------------------------
    // FUNCTION CALL EXPRESSION
    //
    // Parser wraps FunctionCall in this class:
    // Expr.FunctionCallExpr(FunctionCall)
    // -----------------------------
    data class FunctionCallExpr(val call: FunctionCall) : Expr()
}
