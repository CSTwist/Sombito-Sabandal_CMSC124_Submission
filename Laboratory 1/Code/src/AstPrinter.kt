import kotlin.math.floor

// AstPrinter.kt
class AstPrinter {

    fun print(expr: Expr) {
        val result = when (expr) {
            is Expr.Literal  -> literalToString(expr.value)
            is Expr.Grouping -> parenthesize("group", expr.expression)
            is Expr.Unary    -> parenthesize(expr.operator.lexeme, expr.right)
            is Expr.Binary   -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
        }

        // this line actually prints the final result
        println(result)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val sb = StringBuilder()
        sb.append("(").append(name)
        for (e in exprs) {
            sb.append(" ").append(
                when (e) {
                    is Expr.Literal  -> literalToString(e.value)
                    is Expr.Grouping -> parenthesize("group", e.expression)
                    is Expr.Unary    -> parenthesize(e.operator.lexeme, e.right)
                    is Expr.Binary   -> parenthesize(e.operator.lexeme, e.left, e.right)
                }
            )
        }
        sb.append(")")
        return sb.toString()
    }

    private fun literalToString(v: Any?): String =
        when (v) {
            null       -> "nil"
            is String  -> v
            is Double  -> if (v == floor(v)) "${v.toInt()}.0" else v.toString()
            else       -> v.toString()
        }
}
