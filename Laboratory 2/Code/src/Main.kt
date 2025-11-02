// Main.kt
import java.util.Scanner

fun main() {
    val stdin = Scanner(System.`in`)
    val buffer = mutableListOf<String>()

    println("Type/paste code. Type ':run' to parse, ':tokens' to list tokens, ':clear' to reset, ':q' / ':quit' to exit.")

    loop@ while (true) {
        print("> ")
        if (!stdin.hasNextLine()) {
            break@loop
        }

        val raw = stdin.nextLine()

        // Commands are lines that start with ':'
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
                // Use the existing print-style scanner if you want to SEE tokens
                // (keeps your old behavior for debugging)
                Tokenizer.tokenizeBlock(buffer)
                continue@loop
            }

            ":run" -> {
                if (buffer.isEmpty()) {
                    println("[run] Buffer is empty.")
                    continue@loop
                }

                val tokens = Tokenizer.tokenizeToTokens(buffer)
                val parser = Parser(tokens)
                val expr = parser.parse()
                AstPrinter().print(expr)  // directly prints here


                // Reset buffer for next input block
                buffer.clear()
                continue@loop
            }
        }

        // If not a command, treat as source code line and store it
        buffer.add(raw)
    }
}
