import java.io.File

fun main(args: Array<String>) {
    val scriptPath = if (args.isNotEmpty()) args[0] else "Text.txt"

    // Read the content of the script file
    val script = File(scriptPath).readLines()

    // Check if the file exists and if not, print an error and exit
    if (!File(scriptPath).exists()) {
        println("Error: The file $scriptPath does not exist.")
        return
    }

    // Tokenize the script lines
    val tokens = Tokenizer.tokenizeToTokens(script)

    // Parse the tokens into an Abstract Syntax Tree (AST)
    try {
        val parser = Parser(tokens)
        val program = parser.parseProgram()


        val evaluator = Evaluator()
        evaluator.evaluateProgram(program)

    } catch (ex: Exception) {
        println("[Error] Failed to parse or evaluate the script: ${ex.message}")
        ex.printStackTrace()
    }

    println("Program execution completed.")
}