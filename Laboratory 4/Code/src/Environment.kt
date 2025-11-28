// Environment.kt
class Environment(val enclosing: Environment? = null) {

    private val values = mutableMapOf<String, Any?>()

    fun define(name: Token, value: Any?) {
        values[name.lexeme] = value
    }

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


    fun isDefined(name: Token): Boolean {
        return values.containsKey(name.lexeme)
    }

    fun isDefinedAnywhere(name: Token): Boolean {
        if (values.containsKey(name.lexeme)) {
            return true
        }
        return enclosing?.isDefinedAnywhere(name) ?: false
    }


    fun getLocalVariables(): Map<String, Any?> {
        return values.toMap()
    }


    fun clear() {
        values.clear()
    }
}