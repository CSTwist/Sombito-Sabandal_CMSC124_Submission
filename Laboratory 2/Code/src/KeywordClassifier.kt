class KeywordClassifier : LexemeClassifier {
    private val keywords = mapOf(
        "var" to TokenType.VAR,
        "val" to TokenType.VAL,
        "if" to TokenType.IF_CONDITIONAL,
        "else" to TokenType.ELSE_CONDITIONAL,
        "while" to TokenType.WHILE_LOOP,
        "for" to TokenType.FOR_LOOP,
        "fun" to TokenType.FUNCTION_DECLARATION,
        "return" to TokenType.RETURN_CALL,
        "print" to TokenType.FUNCTION,
        "true" to TokenType.BOOLEAN,
        "false" to TokenType.BOOLEAN,
        "nil" to TokenType.NULL,
        "null" to TokenType.NULL,
        "break" to TokenType.LOOP_CONTROL,
        "continue" to TokenType.LOOP_CONTROL,
        "and" to TokenType.LOGICAL_OPERATORS,
        "or" to TokenType.LOGICAL_OPERATORS,
        "not" to TokenType.LOGICAL_OPERATORS
    )

    override fun matches(lexeme: String) = keywords.containsKey(lexeme)

    override fun classify(lexeme: String) = keywords[lexeme]!!
}