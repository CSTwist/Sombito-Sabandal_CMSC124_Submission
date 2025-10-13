// Main.kt
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    val buffer = mutableListOf<String>()

    println("Type/paste code. Type ':run' to evaluate, ':q' / ':quit' to exit.")

    loop@ while (true) {
        if (!scanner.hasNextLine()) {
            // EOF (Ctrl+D) — just exit (no evaluation)
            break@loop
        }

        val raw = scanner.nextLine()
        val cmd = raw.trim()

        when (cmd) {
            ":q", ":quit" -> {
                // exit without evaluating
                break@loop
            }
            ":run" -> {
                if (buffer.isNotEmpty()) {
                    // 1) scanner: collect tokens (no printing)
                    val tokens = Tokenizer.tokenizeToTokens(buffer)

                    // 2) parser: build AST
                    val parser = Parser(tokens)
                    val expr = parser.parse()

                    // 3) AST printer: show the parsed structure
                    val printed = AstPrinter().print(expr)
                    println(printed)

                    buffer.clear()
                } else {
                    println("[run] Buffer is empty.")
                }
                continue@loop
            }
            else -> {
                // keep every line (including blanks) in the buffer
                buffer.add(raw)
            }
        }
    }
}