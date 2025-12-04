// Expr.kt
sealed interface Expr {
    data class Literal(val value: Any?) : Expr
    data class Variable(val name: Token) : Expr
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr
    data class Grouping(val expression: Expr) : Expr
    data class FunctionCallExpr(val call: FunctionCall) : Expr
    data class Percentage(val value: Double) : Expr
    data class Time(val seconds: Int) : Expr

    // Logical expressions: condition and condition, condition or condition
    data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr

    // Unary expressions: !is_dead, -damage
    data class Unary(val operator: Token, val right: Expr) : Expr
}