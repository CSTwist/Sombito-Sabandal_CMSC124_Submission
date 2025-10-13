// Tokenizer.kt
object Tokenizer {

    // ENTRY POINT
    fun tokenizeBlock(lines: List<String>) {
        var multiLineComment = false
        var lineNumber = 1

        for (line in lines) {
            val tokensInLine = scanLine(line, lineNumber)
            multiLineComment = classifyAndPrintTokens(tokensInLine, lineNumber, multiLineComment)
            lineNumber++
        }

        printEOF(lineNumber - 1)

        if (multiLineComment) {
            System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
        }
    }

    // --- Scanning (Lexical Analysis) ---

    // Scan one line into raw lexemes
    private fun scanLine(line: String, lineNumber: Int): MutableList<String> {
        val tokens = mutableListOf<String>()
        var index = 0

        while (index < line.length) {
            val c = line[index]
            when {
                // Ignore whitespace
                c.isWhitespace() -> index++

                // String literal
                c == '"' -> {
                    val (token, newIndex) = scanString(line, index, lineNumber)
                    if (token != null) tokens.add(token)
                    index = newIndex
                }

                // Two-character operators (e.g., <=, &&, /*, //)
                isTwoCharOperator(line, index) -> {
                    val (token, newIndex) = scanTwoCharOperator(line, index)
                    if (token != null) tokens.add(token)
                    index = newIndex
                }

                // Single-character operators (e.g., +, ;)
                isSingleCharOperator(c) -> {
                    tokens.add(c.toString())
                    index++
                }

                // Numbers (digits or starting with . followed by digit)
                c.isDigit() || (c == '.' && index + 1 < line.length && line[index + 1].isDigit()) -> {
                    val (token, newIndex) = scanNumber(line, index)
                    tokens.add(token)
                    index = newIndex
                }

                // Identifiers / Keywords (letters or starting with _)
                c.isLetter() || c == '_' -> {
                    val (token, newIndex) = scanIdentifier(line, index)
                    tokens.add(token)
                    index = newIndex
                }

                // Error case
                else -> {
                    System.err.println("[line $lineNumber] Error: Unexpected character '$c'")
                    index++
                }
            }
        }
        return tokens
    }

    // --- Classification (Token Type Assignment) and Output ---

    // Classify lexemes → tokens and print
    private fun classifyAndPrintTokens(
        tokensInLine: MutableList<String>,
        lineNumber: Int,
        startingCommentState: Boolean
    ): Boolean {
        var multiLineComment = startingCommentState
        var i = 0
        while (i < tokensInLine.size) {
            val lexeme = tokensInLine[i]

            // Comment state machine for block comments
            if (lexeme == "/*") {
                multiLineComment = true
                i++
                continue
            }
            if (lexeme == "*/") {
                multiLineComment = false
                i++
                continue
            }

            val type = classifyLexeme(lexeme)
            val literal = extractLiteral(type, lexeme)

            // Only print tokens if not inside a block comment
            if (!multiLineComment) {
                println("Token(Type=$type, Lexeme=$lexeme, Literal=$literal, Line Number=$lineNumber)")
            }
            i++
        }
        return multiLineComment
    }

    // Decide token type
    private fun classifyLexeme(lexeme: String): TokenType = when (lexeme) {
        // Punctuation & Operators
        "," -> TokenType.COMMA
        ";" -> TokenType.SEMICOLON
        "." -> TokenType.PERIOD
        "(" -> TokenType.LEFT_PAREN
        ")" -> TokenType.RIGHT_PAREN
        "{" -> TokenType.LEFT_BRACE
        "}" -> TokenType.RIGHT_BRACE
        "+" -> TokenType.PLUS
        "-" -> TokenType.MINUS
        "*" -> TokenType.STAR
        "/" -> TokenType.DIVIDE
        "<" -> TokenType.LESS
        "<=" -> TokenType.LESS_EQUAL
        ">" -> TokenType.GREATER
        ">=" -> TokenType.GREATER_EQUAL
        "=" -> TokenType.EQUAL
        "==" -> TokenType.EQUAL_EQUAL
        "!" -> TokenType.BANG
        "!=" -> TokenType.BANG_EQUAL
        "&&" -> TokenType.AND_AND
        "||" -> TokenType.OR_OR

        // Keywords
        "var" -> TokenType.VAR
        "val" -> TokenType.VAL
        "if" -> TokenType.IF_CONDITIONAL
        "else" -> TokenType.ELSE_CONDITIONAL
        "while" -> TokenType.WHILE_LOOP
        "for" -> TokenType.FOR_LOOP
        "fun" -> TokenType.FUNCTION_DECLARATION
        "return" -> TokenType.RETURN_CALL
        "print" -> TokenType.FUNCTION
        "true", "false" -> TokenType.BOOLEAN
        "nil", "null" -> TokenType.NULL
        "break", "continue" -> TokenType.LOOP_CONTROL
        "and", "or", "not" -> TokenType.LOGICAL_OPERATORS

        // Literals and Identifiers
        else -> when {
            lexeme.startsWith("\"") && lexeme.endsWith("\"") -> TokenType.STRING
            isNumber(lexeme) -> TokenType.NUMBER
            else -> TokenType.IDENTIFIER
        }
    }

    // Extract literal value (if any)
    private fun extractLiteral(type: TokenType, lexeme: String): String? = when (type) {
        TokenType.STRING -> lexeme.removePrefix("\"").removeSuffix("\"")
        TokenType.NUMBER -> lexeme
        else -> null
    }

    // Print EOF once at end of block
    private fun printEOF(lastLine: Int) {
        // The original code creates an unused Token object. The println is kept for output format consistency.
        // val eof = Token(TokenType.EOF, "", null, lastLine)
        println("Token(type=EOF, lexeme= NULL, literal= NULL, Line Number=$lastLine)")
    }

    // --- Utility Methods ---

    private fun isNumber(s: String): Boolean {
        if (s.isEmpty()) return false
        var dotSeen = false
        var digitSeen = false
        for (i in s.indices) {
            val c = s[i]
            when {
                c.isDigit() -> digitSeen = true
                c == '.' -> {
                    if (dotSeen) return false
                    dotSeen = true
                }
                else -> return false
            }
        }
        return digitSeen && (s != ".")
    }

    // --- Scanner Helpers ---

    // String literal
    private fun scanString(line: String, start: Int, lineNumber: Int): Pair<String?, Int> {
        var index = start + 1
        while (index < line.length && line[index] != '"') index++

        return if (index >= line.length) {
            System.err.println("[line $lineNumber] Error: Unterminated string")
            // skip rest of line
            Pair(null, line.length)
        } else {
            // consume closing "
            index++
            Pair(line.substring(start, index), index)
        }
    }

    // Two-character operators
    private fun isTwoCharOperator(line: String, index: Int): Boolean {
        if (index + 1 >= line.length) return false
        val twoChars = line.substring(index, index + 2)
        return twoChars in listOf("<=", ">=", "==", "!=", "&&", "||", "//", "/*", "*/")
    }

    private fun scanTwoCharOperator(line: String, start: Int): Pair<String?, Int> {
        val op = line.substring(start, start + 2)
        return if (op == "//") {
            // ignore rest of line (line comment)
            Pair(null, line.length)
        } else {
            Pair(op, start + 2)
        }
    }

    // Single-character operators
    private fun isSingleCharOperator(c: Char): Boolean {
        return c in listOf(',', ';', '.', '(', ')', '{', '}', '+', '-', '*', '/', '<', '>', '=', '!')
    }

    // Numbers
    private fun scanNumber(line: String, start: Int): Pair<String, Int> {
        var index = start
        var hasDot = false
        if (line[index] == '.') {
            hasDot = true
            index++
        }
        while (index < line.length && line[index].isDigit()) index++
        if (index < line.length && line[index] == '.' && !hasDot) {
            if (index + 1 < line.length && line[index + 1].isDigit()) {
                hasDot = true
                index++
                while (index < line.length && line[index].isDigit()) index++
            }
        }
        return Pair(line.substring(start, index), index)
    }

    // Identifiers / keywords
    private fun scanIdentifier(line: String, start: Int): Pair<String, Int> {
        var index = start + 1
        while (index < line.length && (line[index].isLetterOrDigit() || line[index] == '_')) index++
        return Pair(line.substring(start, index), index)
    }
}