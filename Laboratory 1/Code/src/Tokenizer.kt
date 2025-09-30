object Tokenizer {

    fun tokenizeBlock(lines: List<String>, startingCommentState: Boolean): Boolean {
        var multiLineComment = startingCommentState
        var lineNumber = 1

        for (line in lines) {
            var index = 0
            val tokensInLine = mutableListOf<String>()

            while (index < line.length) {
                val c = line[index]
                when {
                    // whitespace
                    c.isWhitespace() -> index++

                    // string literal
                    c == '"' -> {
                        val start = index
                        index++
                        while (index < line.length && line[index] != '"') index++
                        if (index >= line.length) {
                            System.err.println("[line $lineNumber] Error: Unterminated string")
                            break
                        } else {
                            index++
                            tokensInLine.add(line.substring(start, index))
                        }
                    }

                    // two-character operators
                    index + 1 < line.length && (line.substring(index, index + 2) in listOf(
                        "<=", ">=", "==", "!=", "&&", "||", "//", "/*", "*/"
                    )) -> {
                        val op = line.substring(index, index + 2)
                        if (op == "//") break
                        tokensInLine.add(op)
                        index += 2
                    }

                    // single-character operators & delimiters (NOW includes { and })
                    c in listOf(',', ';', '.', '(', ')', '{', '}', '+', '-', '*', '/', '<', '>', '=', '!') -> {
                        tokensInLine.add(c.toString())
                        index++
                    }

                    // numbers
                    c.isDigit() || (c == '.' && index + 1 < line.length && line[index + 1].isDigit()) -> {
                        val start = index
                        var hasDot = false
                        if (c == '.') { hasDot = true; index++ }
                        while (index < line.length && line[index].isDigit()) index++
                        if (index < line.length && line[index] == '.' && !hasDot) {
                            if (index + 1 < line.length && line[index + 1].isDigit()) {
                                hasDot = true; index++
                                while (index < line.length && line[index].isDigit()) index++
                            }
                        }
                        tokensInLine.add(line.substring(start, index))
                    }

                    // identifiers / keywords (allow _)
                    c.isLetter() || c == '_' -> {
                        val start = index; index++
                        while (index < line.length && (line[index].isLetterOrDigit() || line[index] == '_')) index++
                        tokensInLine.add(line.substring(start, index))
                    }

                    else -> {
                        System.err.println("[line $lineNumber] Error: Unexpected character '$c'")
                        index++
                    }
                }
            }

            // classify & print
            var i = 0
            while (i < tokensInLine.size) {
                val lexeme = tokensInLine[i]

                if (lexeme == "/*") { multiLineComment = true; i++; continue }
                if (lexeme == "*/") { multiLineComment = false; i++; continue }

                val type = when (lexeme) {
                    // punctuation & operators
                    "," -> "COMMA"
                    ";" -> "SEMICOLON"
                    "." -> "PERIOD"
                    "(" -> "LEFT_PAREN"
                    ")" -> "RIGHT_PAREN"
                    "{" -> "LEFT_BRACE"
                    "}" -> "RIGHT_BRACE"
                    "+" -> "PLUS"
                    "-" -> "MINUS"
                    "*" -> "STAR"
                    "/" -> "DIVIDE"
                    "<" -> "LESS"
                    "<=" -> "LESS_EQUAL"
                    ">" -> "GREATER"
                    ">=" -> "GREATER_EQUAL"
                    "=" -> "EQUAL"
                    "==" -> "EQUAL_EQUAL"
                    "!" -> "BANG"
                    "!=" -> "BANG_EQUAL"
                    "&&" -> "AND_AND"
                    "||" -> "OR_OR"

                    // keywords
                    "var" -> "VAR"
                    "val" -> "VAL"
                    "if" -> "IF_CONDITIONAL"
                    "else" -> "ELSE_CONDITIONAL"
                    "while" -> "WHILE_LOOP"
                    "for" -> "FOR_LOOP"
                    "fun" -> "FUNCTION_DECLARATION"
                    "return" -> "RETURN_CALL"
                    "print" -> "FUNCTION"
                    "true", "false" -> "BOOLEAN"
                    "nil", "null" -> "NULL"
                    "break", "continue" -> "LOOP_CONTROL"
                    "and", "or", "not" -> "LOGICAL_OPERATORS"

                    // literals / identifiers
                    else -> when {
                        lexeme.startsWith("\"") && lexeme.endsWith("\"") -> "STRING"
                        isNumber(lexeme) -> "NUMBER"
                        else -> "IDENTIFIER"
                    }
                }

                val literal = when (type) {
                    "STRING" -> lexeme.removePrefix("\"").removeSuffix("\"")
                    "NUMBER" -> lexeme
                    else -> "null"
                }

                if (!multiLineComment) {
                    println("Token(type=$type , lexeme=$lexeme , literal=$literal , line =$lineNumber)")
                }
                i++
            }

            lineNumber++
        }

        // one EOF at the end of the whole block
        println("Token(type=EOF , lexeme= , literal=null , line =${lineNumber - 1})")

        if (multiLineComment) {
            System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
        }
        return multiLineComment
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
}
