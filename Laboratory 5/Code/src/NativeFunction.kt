// NativeFunction.kt
interface NativeFunction {
    fun arity(): Int
    fun call(context: ExecutionContext, args: List<Any?>): Any?
    fun name(): String
}

// ---- Helper: unwrap numeric arguments safely ----

fun num(arg: Any?): Double {
    return when (arg) {
        is Double -> arg
        is Int -> arg.toDouble()
        else -> error("Expected number, got $arg")
    }
}
