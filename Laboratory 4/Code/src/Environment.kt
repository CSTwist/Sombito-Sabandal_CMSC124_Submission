// Environment.kt
class Environment(val enclosing: Environment? = null) {

    private val values = mutableMapOf<String, Any?>()

    /**
     * Define a new variable in this environment
     */
    fun define(name: Token, value: Any?) {
        values[name.lexeme] = value
    }

    /**
     * Get the value of a variable, searching up the scope chain if needed
     */
    fun get(name: Token): Any? {
        val key = name.lexeme
        if (values.containsKey(key)) {
            return values[key]
        }
        if (enclosing != null) {
            return enclosing.get(name)
        }
        throw RuntimeException("Undefined variable '${name.lexeme}' at line ${name.line}.")
    }

    /**
     * Assign a value to an existing variable, searching up the scope chain if needed
     */
    fun assign(name: Token, value: Any?) {
        val key = name.lexeme
        if (values.containsKey(key)) {
            values[key] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeException("Cannot assign to undefined variable '${name.lexeme}' at line ${name.line}.")
    }

    /**
     * Check if a variable exists in this environment (local scope only)
     */
    fun isDefined(name: Token): Boolean {
        return values.containsKey(name.lexeme)
    }

    /**
     * Check if a variable exists anywhere in the scope chain
     */
    fun isDefinedAnywhere(name: Token): Boolean {
        if (values.containsKey(name.lexeme)) {
            return true
        }
        return enclosing?.isDefinedAnywhere(name) ?: false
    }

    /**
     * Get all variables defined in this environment (local scope only)
     */
    fun getLocalVariables(): Map<String, Any?> {
        return values.toMap()
    }

    /**
     * Clear all variables in this environment
     */
    fun clear() {
        values.clear()
    }
}