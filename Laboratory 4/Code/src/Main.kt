// Main.kt
import java.util.Scanner

fun main() {
    val stdin = Scanner(System.`in`)
    val buffer = mutableListOf<String>()

    println("Game DSL Parser - Commands:")
    println("  :tokens   - Show tokenized output")
    println("  :parse    - Parse and show AST")
    println("  :evaluate - Evaluate an expression")
    println("  :run      - Parse and evaluate full program")
    println("  :clear    - Clear buffer")
    println("  :q/:quit  - Exit")
    println()

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
                    println("[parse] Buffer is empty.")
                    continue@loop
                }

                try {
                    val tokens = Tokenizer.tokenizeToTokens(buffer)
                    val parser = Parser(tokens)
                    val program = parser.parseProgram()
                    AstPrinter().print(program)
                } catch (ex: Exception) {
                    println("[parse] Failed: ${ex.message}")
                    ex.printStackTrace()
                }

                buffer.clear()
                continue@loop
            }

            ":evaluate" -> {
                if (buffer.isEmpty()) {
                    println("[evaluate] Buffer is empty.")
                    continue@loop
                }

                try {
                    val tokens = Tokenizer.tokenizeToTokens(buffer)
                    val parser = Parser(tokens)
                    val expr = parser.parseExpression()
                    val evaluator = Evaluator()
                    val result = evaluator.evaluate(expr)
                    println("Result: $result")
                } catch (ex: Exception) {
                    println("[evaluate] Failed: ${ex.message}")
                    ex.printStackTrace()
                }

                buffer.clear()
                continue@loop
            }

            ":run" -> {
                if (buffer.isEmpty()) {
                    println("[run] Buffer is empty.")
                    continue@loop
                }

                try {
                    val tokens = Tokenizer.tokenizeToTokens(buffer)
                    val parser = Parser(tokens)
                    val program = parser.parseProgram()
                    val evaluator = Evaluator()
                    evaluator.evaluateProgram(program)
                } catch (ex: Exception) {
                    println("[run] Failed: ${ex.message}")
                    ex.printStackTrace()
                }

                buffer.clear()
                continue@loop
            }
        }

        // If not a command, treat as source code line
        buffer.add(raw)
    }

    println("Goodbye!")
}