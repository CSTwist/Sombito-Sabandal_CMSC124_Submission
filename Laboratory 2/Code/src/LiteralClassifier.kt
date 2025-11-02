class LiteralClassifier : LexemeClassifier {
    override fun matches(lexeme: String): Boolean =
        (lexeme.startsWith("\"") && lexeme.endsWith("\"")) || isNumber(lexeme)

    override fun classify(lexeme: String): TokenType =
        when {
            lexeme.startsWith("\"") && lexeme.endsWith("\"") -> TokenType.STRING
            isNumber(lexeme) -> TokenType.NUMBER
            else -> TokenType.IDENTIFIER
        }

    private fun isNumber(s: String): Boolean {
        if (s.isEmpty()) return false
        var dotSeen = false
        var digitSeen = false
        for (c in s) {
            when {
                c.isDigit() -> digitSeen = true
                c == '.' -> if (dotSeen) return false else dotSeen = true
                else -> return false
            }
        }
        return digitSeen && (s != ".")
    }
}