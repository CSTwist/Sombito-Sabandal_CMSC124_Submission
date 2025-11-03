// Main.kt
import java.util.Scanner

fun main() {
    val stdin = Scanner(System.`in`)
    val buffer = mutableListOf<String>()

    println("Type/paste code. Commands: ':parse' to parse, ':tokens' to list tokens, ':clear' to reset, ':q' / ':quit' to exit.")

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

                val tokens = Tokenizer.tokenizeToTokens(buffer)
                val parser = Parser(tokens)
                val program = parser.parseProgram()

                println("=== Parsed Program AST ===")
                AstPrinter().print(program)  // <-- now prints the whole program

                buffer.clear()
                continue@loop
            }
        }

        // If not a command, treat as source code line
        buffer.add(raw)
    }
}