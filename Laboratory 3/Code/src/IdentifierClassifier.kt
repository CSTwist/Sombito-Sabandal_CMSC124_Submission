class IdentifierClassifier : LexemeClassifier {
    override fun matches(lexeme: String): Boolean = true // fallback
    override fun classify(lexeme: String): TokenType = TokenType.IDENTIFIER
}
