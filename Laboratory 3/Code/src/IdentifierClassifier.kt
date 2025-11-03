class IdentifierClassifier : LexemeClassifier {
    override fun matches(lexeme: String): Boolean {
        if (lexeme.indexOf("OA_")== 0){
            return true
        }
        return false
    }
    override fun classify(lexeme: String): TokenType = TokenType.IDENTIFIER
}
