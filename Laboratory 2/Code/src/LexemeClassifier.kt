interface LexemeClassifier {
    fun matches(lexeme: String): Boolean
    fun classify(lexeme: String): TokenType
}
