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
                    // Assuming 'Tokenizer.tokenizeBlock' is defined elsewhere
                    Tokenizer.tokenizeBlock(buffer)
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