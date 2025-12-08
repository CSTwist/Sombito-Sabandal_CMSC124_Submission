// Token.kt
//
// Represents a lexical token produced by the Tokenizer.
// Clean, consistent, and compatible with the Parser, Evaluator, and AST.
//

data class Token(
    val type: TokenType,     // The category of the token (e.g. IDENTIFIER, NUMBER, LEFT_BRACE)
    val lexeme: String,      // The raw text of the token as found in source
    val literal: Any?,       // The processed/parsed literal value (if applicable)
    val line: Int            // Line number for error messages
) {
    override fun toString(): String {
        return "Token(type=$type, lexeme='$lexeme', literal=$literal, line=$line)"
    }
}
