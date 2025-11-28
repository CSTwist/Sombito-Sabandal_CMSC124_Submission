// Expr.kt
sealed interface Expr {
    data class Literal(val value: Any?) : Expr
    data class Variable(val name: Token) : Expr
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr
    data class Grouping(val expression: Expr) : Expr
    data class FunctionCallExpr(val call: FunctionCall) : Expr
    data class Percentage(val value: Double) : Expr
    data class Time(val seconds: Int) : Expr
}