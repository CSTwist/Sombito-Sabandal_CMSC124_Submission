// Main.kt
//
// Entry point for the DSL interpreter.
// Reads the source file, tokenizes, parses, evaluates, and prints results.

fun main(args: Array<String>) {

    // -----------------------------------------
    // 0. CHECK INPUT ARGUMENTS
    // -----------------------------------------
    if (args.isEmpty()) {
        println("Usage: dsl <source.dsl>")
        return
    }

    val path = args[0]

    // -----------------------------------------
    // 1. READ SOURCE FILE
    // -----------------------------------------
    val lines: List<String> = try {
        java.io.File(path).readLines()
    } catch (e: Exception) {
        println("Error reading file '$path': ${e.message}")
        return
    }

    // -----------------------------------------
    // 2. TOKENIZE
    // -----------------------------------------
    val tokens = try {
        Tokenizer.tokenizeToTokens(lines)
    } catch (e: Exception) {
        println("Tokenizer error: ${e.message}")
        return
    }

    // Optional token debug:
    // tokens.forEach { println(it) }

    // -----------------------------------------
    // 3. PARSE → AST
    // -----------------------------------------
    val parser = Parser(tokens)
    val program: Program = try {
        parser.parseProgram()
    } catch (e: Exception) {
        println("Parser error: ${e.message}")
        return
    }

    // -----------------------------------------
    // 4. OPTIONAL AST PRINT
    // -----------------------------------------
    println("=== AST OUTPUT ===")
    AstPrinter().print(program)
    println("=== END AST ===\n")

    // -----------------------------------------
    // 5. EVALUATE PROGRAM
    // -----------------------------------------
    val evaluator = Evaluator()
    try {
        evaluator.evaluate(program)
    } catch (e: Exception) {
        println("Runtime error: ${e.message}")
        return
    }

    println("\nProgram executed successfully.")
}
