// RuntimeError.kt
class RuntimeError(val token: Token, message: String) : RuntimeException(message) {
    companion object {
        fun report(token: Token, message: String): Nothing {
            val line = token.line
            println("[line $line] Runtime error: $message.")
            throw RuntimeError(token, message)
        }
    }
}
