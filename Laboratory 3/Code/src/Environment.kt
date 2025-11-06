// Environment.kt
class Environment(val enclosing: Environment? = null) {

    private data class Entry(val value: Any?, val isConst: Boolean)
    private val values = mutableMapOf<String, Entry>()

    fun define(name: Token, value: Any?, isConst: Boolean = false) {
        values[name.lexeme] = Entry(value, isConst)
    }

    fun get(name: Token): Any? {
        val key = name.lexeme
        if (values.containsKey(key)) return values[key]!!.value
        if (enclosing != null) return enclosing.get(name)
        RuntimeError.report(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        val key = name.lexeme
        if (values.containsKey(key)) {
            val e = values[key]!!
            if (e.isConst) RuntimeError.report(name, "Cannot assign to constant '${name.lexeme}'.")
            values[key] = e.copy(value = value)
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        RuntimeError.report(name, "Undefined variable '${name.lexeme}'.")
    }
}
