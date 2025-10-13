// AstPrinter.kt
class AstPrinter {

    fun print(expr: Expr): String = when (expr) {
        is Expr.Literal  -> literalToString(expr.value)
        is Expr.Grouping -> parenthesize("group", expr.expression)
        is Expr.Unary    -> parenthesize(expr.operator.lexeme, expr.right)
        is Expr.Binary   -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val sb = StringBuilder()
        sb.append("(").append(name)
        for (e in exprs) {
            sb.append(" ").append(print(e))
        }
        sb.append(")")
        return sb.toString()
    }

    private fun literalToString(v: Any?): String =
        when (v) {
            null       -> "nil"
            is String  -> v
            is Double  -> if (v == Math.floor(v)) "${v.toInt()}.0" else v.toString()
            else       -> v.toString()
        }
}