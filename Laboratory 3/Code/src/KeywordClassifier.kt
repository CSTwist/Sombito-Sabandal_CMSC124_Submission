class KeywordClassifier : LexemeClassifier {
    // Conyo-flavored keyword table (with a few legacy fallbacks)
    private val keywords = mapOf(
        // Vars
        "lagay" to TokenType.VAR,                 // mutable (var)
        "peg" to TokenType.CONST,                   // immutable (val)

        // Conditionals
        "kung" to TokenType.IF_CONDITIONAL,       // if
        "kundi" to TokenType.ELSE_CONDITIONAL,    // else

        // Loops
        "habang" to TokenType.WHILE_LOOP,         // while
        "para" to TokenType.FOR_LOOP,             // for

        // Functions
        "ganap" to TokenType.FUNCTION_DECLARATION,// fun
        "balik" to TokenType.RETURN_CALL,         // return
        "chika" to TokenType.FUNCTION,            // print/log
        "other" to TokenType.OTHER,

    // Literals
        "yass" to TokenType.TRUE,              // true
        "noh" to TokenType.FALSE,               // false
        "wala" to TokenType.NULL,                 // null

        // Word logical operators
        "hindi" to TokenType.NOT_WORD,   // NOT
        "tsaka" to TokenType.AND_WORD,   // AND
        "or" to TokenType.OR_WORD,       // OR (kept as-is)



    )

    override fun matches(lexeme: String) = keywords.containsKey(lexeme)

    override fun classify(lexeme: String) = keywords[lexeme]!!
}
