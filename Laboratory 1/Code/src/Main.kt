// Main.kt
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    val buffer = mutableListOf<String>()     // collect lines for the current block
    var multiLineComment = false
    var emptyLineCount = 0                   // counts consecutive blank lines

    println(
        "Paste/enter code. Single blank lines are kept. " +
                "Press ENTER twice (double blank line) to evaluate. " +
                "Quit with :q / :quit."
    )

    loop@ while (true) {
        if (!scanner.hasNextLine()) {
            // EOF: if there is a pending block, evaluate once before exiting
            if (buffer.isNotEmpty()) {
                multiLineComment = Tokenizer.tokenizeBlock(buffer, multiLineComment)
                buffer.clear()
            }
            break@loop
        }

        val raw = scanner.nextLine()
        val cmd = raw.trim()

        // explicit quit
        if (cmd == ":q" || cmd == ":quit") {
            // optional: evaluate pending block before quitting (comment out if not desired)
            if (buffer.isNotEmpty()) {
                multiLineComment = Tokenizer.tokenizeBlock(buffer, multiLineComment)
                buffer.clear()
            }
            break@loop
        }

        if (raw.isBlank()) {
            emptyLineCount += 1
            if (emptyLineCount >= 2) {
                // double blank line → evaluate buffered block
                if (buffer.isNotEmpty()) {
                    multiLineComment = Tokenizer.tokenizeBlock(buffer, multiLineComment)
                    buffer.clear()
                }
                emptyLineCount = 0
                println("---- Ready for next block ----")
            } else {
                // keep a single blank line inside the block
                buffer.add("")
            }
            continue@loop
        } else {
            // non-blank resets the counter and is appended to the block
            emptyLineCount = 0
            buffer.add(raw)
        }
    }
}


