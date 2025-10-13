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

    // --- NEW: return tokens for the parser (no printing) ---
    fun tokenizeToTokens(lines: List<String>): List<Token> {
        val out = mutableListOf<Token>()
        var multiLineComment = false
        var lineNumber = 1

        for (line in lines) {
            val lexemes = scanLine(line, lineNumber) // uses your existing private helper
            var i = 0
            while (i < lexemes.size) {
                val lex = lexemes[i]

                // block comment state
                if (lex == "/*") { multiLineComment = true; i++; continue }
                if (lex == "*/") { multiLineComment = false; i++; continue }
                if (multiLineComment) { i++; continue }

                val type = classifyLexeme(lex)
                val literal = extractLiteral(type, lex)
                out.add(Token(type, lex, literal, lineNumber))
                i++
            }
            lineNumber++
        }

        // Always end with EOF on the last processed line
        out.add(Token(TokenType.EOF, "", null, (lineNumber - 1).coerceAtLeast(1)))

        if (multiLineComment) {
            System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
        }

        return out
    }


    // Scan one line into raw lexemes
    private fun scanLine(line: String, lineNumber: Int): MutableList<String> {
        val tokens = mutableListOf<String>()
        var index = 0

        while (index < line.length) {
            val c = line[index]
            when {
                c.isWhitespace() -> index++

                c == '"' -> {
                    val (token, newIndex) = scanString(line, index, lineNumber)
                    if (token != null) tokens.add(token)
                    index = newIndex
                }

                isTwoCharOperator(line, index) -> {
                    val (token, newIndex) = scanTwoCharOperator(line, index)
                    if (token != null) tokens.add(token)
                    index = newIndex
                }

                isSingleCharOperator(c) -> {
                    tokens.add(c.toString())
                    index++
                }

                c.isDigit() || (c == '.' && index + 1 < line.length && line[index + 1].isDigit()) -> {
                    val (token, newIndex) = scanNumber(line, index)
                    tokens.add(token)
                    index = newIndex
                }

                c.isLetter() || c == '_' -> {
                    val (token, newIndex) = scanIdentifier(line, index)
                    tokens.add(token)
                    index = newIndex
                }

                else -> {
                    System.err.println("[line $lineNumber] Error: Unexpected character '$c'")
                    index++
                }
            }
        }
        return tokens
    }


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

            // comment state machine
            if (lexeme == "/*") { multiLineComment = true; i++; continue }
            if (lexeme == "*/") { multiLineComment = false; i++; continue }

            val type = classifyLexeme(lexeme)
            val literal = extractLiteral(type, lexeme)

            if (!multiLineComment) {
                println("Token(Type=$type, Lexeme=$lexeme, Literal=$literal, Line Number=$lineNumber)")
            }
            i++
        }
        return multiLineComment
    }

    // Decide token type
    private fun classifyLexeme(lexeme: String): TokenType = when (lexeme) {
        // punctuation & operators
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

        // keywords
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
        val eof = Token(TokenType.EOF, "", null, lastLine)
        println("Token(type=EOF, lexeme= NULL, literal= NULL, Line Number=$lastLine)")
    }

    private fun isNumber(s: String): Boolean {
        if (s.isEmpty()) return false
        var dotSeen = false
        var digitSeen = false
        for (i in s.indices) {
            val c = s[i]
            when {
                c.isDigit() -> digitSeen = true
                c == '.' -> { if (dotSeen) return false; dotSeen = true }
                else -> return false
            }
        }
        return digitSeen && (s != ".")
    }

    // --- String literal ---
    private fun scanString(line: String, start: Int, lineNumber: Int): Pair<String?, Int> {
        var index = start + 1
        while (index < line.length && line[index] != '"') index++
        return if (index >= line.length) {
            System.err.println("[line $lineNumber] Error: Unterminated string")
            Pair(null, line.length) // skip rest of line
        } else {
            index++ // consume closing "
            Pair(line.substring(start, index), index)
        }
    }

    // --- Two-character operators ---
    private fun isTwoCharOperator(line: String, index: Int): Boolean {
        if (index + 1 >= line.length) return false
        val twoChars = line.substring(index, index + 2)
        return twoChars in listOf("<=", ">=", "==", "!=", "&&", "||", "//", "/*", "*/")
    }

    private fun scanTwoCharOperator(line: String, start: Int): Pair<String?, Int> {
        val op = line.substring(start, start + 2)
        return if (op == "//") {
            Pair(null, line.length) // ignore rest of line (comment)
        } else {
            Pair(op, start + 2)
        }
    }

    // --- Single-character operators ---
    private fun isSingleCharOperator(c: Char): Boolean {
        return c in listOf(',', ';', '.', '(', ')', '{', '}', '+', '-', '*', '/', '<', '>', '=', '!')
    }

    // --- Numbers ---
    private fun scanNumber(line: String, start: Int): Pair<String, Int> {
        var index = start
        var hasDot = false
        if (line[index] == '.') { hasDot = true; index++ }
        while (index < line.length && line[index].isDigit()) index++
        if (index < line.length && line[index] == '.' && !hasDot) {
            if (index + 1 < line.length && line[index + 1].isDigit()) {
                hasDot = true; index++
                while (index < line.length && line[index].isDigit()) index++
            }
        }
        return Pair(line.substring(start, index), index)
    }

    // --- Identifiers / keywords ---
    private fun scanIdentifier(line: String, start: Int): Pair<String, Int> {
        var index = start + 1
        while (index < line.length && (line[index].isLetterOrDigit() || line[index] == '_')) index++
        return Pair(line.substring(start, index), index)
    }
}