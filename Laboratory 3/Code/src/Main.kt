// Main.kt
import java.util.Scanner

fun main() {
    val stdin = Scanner(System.`in`)
    val buffer = mutableListOf<String>()

    println("Type/paste code. Commands: ':evaluate' to evaluate, ':tokens' to list tokens, ':clear' to reset, ':q' / ':quit' to exit.")

    loop@ while (true) {
        print("> ")
        if (!stdin.hasNextLine()) break@loop

        val raw = stdin.nextLine()
        val cmd = raw.trim()

        when (cmd) {
            ":q", ":quit" -> break@loop

            ":clear" -> {
                buffer.clear()
                println("[ok] Buffer cleared.")
                continue@loop
            }

            ":tokens" -> {
                if (buffer.isEmpty()) {
                    println("[tokens] Buffer is empty.")
                    continue@loop
                }
                Tokenizer.tokenizeBlock(buffer)
                continue@loop
            }

            ":parse" -> {
                if (buffer.isEmpty()) {
                    println("[evaluate] Buffer is empty.")
                    continue@loop
                }

                val tokens = Tokenizer.tokenizeToTokens(buffer)
                val parser = Parser(tokens)
                val program = parser.parseProgram()
                AstPrinter().print(program)

                buffer.clear()
                continue@loop
            }

            ":evaluate" -> {
                if (buffer.isEmpty()) {
                    println("[evaluate] Buffer is empty.")
                    continue@loop
                }

                val tokens = Tokenizer.tokenizeToTokens(buffer)
                val parser = Parser(tokens)
                try {
                    val expr = parser.parseExpression()
                    val evaluator = Evaluator()
                    val value = evaluator.javaClass
                        .getDeclaredMethod("evalExpr", Expr::class.java)
                        .apply { isAccessible = true }
                        .invoke(evaluator, expr)

                    println(value)
                } catch (ex: Exception) {
                    println("[evaluate] Failed: ${ex.message}")
                }

                buffer.clear()
                continue@loop
            }
        }

        // If not a command, treat as source code line
        buffer.add(raw)
    }
}
