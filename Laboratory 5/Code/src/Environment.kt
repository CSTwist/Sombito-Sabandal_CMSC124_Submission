// Environment.kt
//
// Simple lexical environment used by the Evaluator.
// Stores variables, functions, hero objects, and all runtime values produced by the DSL.
//

class Environment(
    private val enclosing: Environment? = null
) {

    private val values: MutableMap<String, Any?> = mutableMapOf()

    // ---------------------------------------------------------
    // DEFINE — Introduces a new binding in the current scope
    // ---------------------------------------------------------
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    // ---------------------------------------------------------
    // ASSIGN — Rebinds an existing variable (searches upwards)
    // ---------------------------------------------------------
    fun assign(nameToken: Token, value: Any?) {
        val name = nameToken.lexeme

        if (values.containsKey(name)) {
            values[name] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(nameToken, value)
            return
        }

        runtimeError(nameToken, "Undefined variable '$name'.")
    }

    // ---------------------------------------------------------
    // GET — Retrieves a value from this environment or parents
    // ---------------------------------------------------------
    fun get(nameToken: Token): Any? {
        val name = nameToken.lexeme

        if (values.containsKey(name)) return values[name]

        if (enclosing != null) return enclosing.get(nameToken)

        runtimeError(nameToken, "Undefined variable '$name'.")
        return null
    }

    // ---------------------------------------------------------
    // CHILD — Creates a nested scope
    // ---------------------------------------------------------
    fun createChild(): Environment = Environment(this)

    // ---------------------------------------------------------
    // DEBUG / PRINT SUPPORT
    // ---------------------------------------------------------
    fun debugDump(title: String = "Environment") {
        println("=== $title ===")
        dumpRecursive(0)
        println("===================")
    }

    private fun dumpRecursive(depth: Int) {
        val indent = "  ".repeat(depth)
        values.forEach { (k, v) ->
            println("$indent$k = ${prettyValue(v)}")
        }
        enclosing?.dumpRecursive(depth + 1)
    }

    private fun prettyValue(v: Any?): String =
        when (v) {
            null -> "nil"
            is Map<*, *> -> v.entries.joinToString(prefix = "{", postfix = "}") { (k, value) ->
                "$k=${prettyValue(value)}"
            }
            is List<*> -> v.joinToString(prefix = "[", postfix = "]") { prettyValue(it) }
            else -> v.toString()
        }

    private fun runtimeError(token: Token, msg: String): Nothing {
        throw RuntimeException("[line ${token.line}] Error at '${token.lexeme}': $msg")
    }
}
