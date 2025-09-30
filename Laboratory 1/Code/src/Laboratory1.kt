import java.util.Scanner

class Token(
    var type: String = "",
    var lexeme: String = "",
    var literal: String = "",
    var line: String = ""
)

fun main() {
    val scanner = Scanner(System.`in`)
    println(">")

    var lineNumber = 1
    var multiLineComment = false

    while (true) {
        val line = if (scanner.hasNextLine()) scanner.nextLine() else break
        if (line.isBlank()) break

        var index = 0
        val tokensInLine = mutableListOf<String>()

        while (index < line.length) {
            val c = line[index]

            when {
                // whitespace
                c.isWhitespace() -> index++

                // string literal with unterminated-string check
                c == '"' -> {
                    val start = index
                    index++
                    while (index < line.length && line[index] != '"') index++
                    if (index >= line.length) {
                        System.err.println("[line $lineNumber] Error: Unterminated string")
                        // don't add a STRING token; skip rest of line after the opening quote
                        break
                    } else {
                        index++ // consume closing "
                        tokensInLine.add(line.substring(start, index))
                    }
                }

                // two-character operators (now includes !=, &&, ||)
                index + 1 < line.length && (line.substring(index, index + 2) in listOf(
                    "<=", ">=", "==", "!=", "&&", "||", "//", "/*", "*/"
                )) -> {
                    val op = line.substring(index, index + 2)
                    if (op == "//") {
                        // stop scanning the rest of this line immediately
                        break
                    }
                    tokensInLine.add(op)
                    index += 2
                }

                // single-character operators and delimiters (added '!')
                c in listOf(',', ';', '.', '(', ')', '+', '-', '*', '/', '<', '>', '=', '!') -> {
                    tokensInLine.add(c.toString())
                    index++
                }

                // number (integer or decimal starting with '.')
                c.isDigit() || (c == '.' && index + 1 < line.length && line[index + 1].isDigit()) -> {
                    val start = index
                    var hasDot = false
                    if (c == '.') {
                        hasDot = true
                        index++
                    }
                    while (index < line.length && line[index].isDigit()) index++
                    if (index < line.length && line[index] == '.' && !hasDot) {
                        // require at least one digit after the dot to count as decimal
                        if (index + 1 < line.length && line[index + 1].isDigit()) {
                            hasDot = true
                            index++
                            while (index < line.length && line[index].isDigit()) index++
                        }
                    }
                    tokensInLine.add(line.substring(start, index))
                }

                // identifier/keyword (allow underscores)
                c.isLetter() || c == '_' -> {
                    val start = index
                    index++
                    while (index < line.length && (line[index].isLetterOrDigit() || line[index] == '_')) index++
                    tokensInLine.add(line.substring(start, index))
                }

                else -> {
                    // unknown character (report but continue)
                    System.err.println("[line $lineNumber] Error: Unexpected character '$c'")
                    index++
                }
            }
        }

        // Map lexemes → token types & emit for THIS line (then EOF), skipping block-comment content
        var i = 0
        while (i < tokensInLine.size) {
            val lexeme = tokensInLine[i]

            // block comment state machine (minimal change: keep your approach)
            if (lexeme == "/*") {
                multiLineComment = true
                i++
                continue
            } else if (lexeme == "*/") {
                multiLineComment = false
                i++
                continue
            }

            val type = when (lexeme) {
                "var" -> "VAR"
                "val" -> "VAL"
                "," -> "COMMA"
                ";" -> "SEMICOLON"
                "." -> "PERIOD"
                "=" -> "EQUAL"
                "==" -> "EQUAL_EQUAL"
                "!" -> "BANG"
                "!=" -> "BANG_EQUAL"
                "-" -> "MINUS"
                "+" -> "PLUS"
                "/" -> "DIVIDE"
                "*" -> "STAR"
                ")" -> "RIGHT_PAREN"
                "(" -> "LEFT_PAREN"
                "<=" -> "LESS_EQUAL"
                ">=" -> "GREATER_EQUAL"
                "<" -> "LESS"
                ">" -> "GREATER"
                "&&" -> "AND_AND"
                "||" -> "OR_OR"
                // comments are handled above; no COMMENT tokens emitted
                "if" -> "IF_CONDITIONAL"
                "else" -> "ELSE_CONDITIONAL"
                "while" -> "WHILE_LOOP"
                "for" ->  "FOR_LOOP"
                "fun" -> "FUNCTION_DECLARATION"
                "return" -> "RETURN_CALL"
                "nil", "null" -> "NULL"
                "break", "continue" -> "LOOP_CONTROL"
                "print" -> "FUNCTION"
                "and", "or", "not" -> "LOGICAL_OPERATORS"
                "true", "false" -> "BOOLEAN"
                else -> when {
                    lexeme.startsWith("\"") && lexeme.endsWith("\"") -> "STRING"
                    isNumber(lexeme) -> "NUMBER"
                    else -> "IDENTIFIER"
                }
            }

            val literal = when (type) {
                "NUMBER" -> lexeme
                "STRING" -> lexeme.removePrefix("\"").removeSuffix("\"")
                else -> "null"
            }

            // Only add if not inside a block comment
            if (!multiLineComment) {
                println("Token(type=$type , lexeme=$lexeme , literal=$literal , line =$lineNumber)")
            }

            i++
        }

        // Per-line EOF to match lab output
        println("Token(type=EOF , lexeme= , literal=null , line =$lineNumber)")

        lineNumber++
    }

    // If a block comment was never closed, warn once at the end
    if (multiLineComment) {
        System.err.println("[line ${lineNumber - 1}] Error: Unterminated block comment (missing */)")
    }
}

fun isNumber(s: String): Boolean {
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
    // Allow forms like "123", "123.45", ".5" (but not just ".")
    return digitSeen && !(s == ".")
}
