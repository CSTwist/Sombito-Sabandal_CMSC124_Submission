// Parser.kt
class Parser(private val tokens: List<Token>) {

    private var current = 0

    // ---- Entry Point ----
    fun parseProgram(): Program {
        val decls = mutableListOf<Decl>()
        while (!isAtEnd()) {
            decls.add(declaration())
        }
        return Program(decls)
    }

    fun parseExpression(): Expr {
        val expr = expression()
        if (!isAtEnd()) {
            error(peek(), "Unexpected tokens after expression.")
        }
        return expr
    }

    // ---- Top-level Declarations ----
    private fun declaration(): Decl {
        return when {
            match(TokenType.FUNCTION_DECLARATION) -> funDecl()
            match(TokenType.VAR) -> varDecl()
            match(TokenType.CONST) -> constDecl()
            match(TokenType.ARAY) -> arayDecl()
            match(TokenType.ENUM) -> enumDecl()
            else -> {
                error(peek(), "Unexpected top-level statement.")
                synchronize()
                // Dummy declaration so parsing can continue
                Decl.VarDecl(
                    Token(TokenType.IDENTIFIER, "error", null, peek().line),
                    Expr.Literal(null)
                )
            }
        }
    }

    // ganap IDENTIFIER ( [ ParamList ] ) Block
    // ganap [ DataType ] IDENTIFIER ( [ ParamList ] ) Block [ other Block ]
    private fun funDecl(): Decl.FunDecl {
        var returnType: Token? = null

        // If there are two consecutive identifiers, first is return type.
        if (check(TokenType.IDENTIFIER)) {
            val next = tokens.getOrNull(current + 1)
            if (next != null && next.type == TokenType.IDENTIFIER) {
                returnType = advance()
            }
        }

        val name = consume(TokenType.IDENTIFIER, "Expect function name after 'ganap'.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")
        val params = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")

        // ✅ must consume '{' before block
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.")
        val body = blockStmt()

        // Optional 'other' block
        var otherBlock: Stmt.Block? = null
        if (match(TokenType.OTHER)) {
            if (returnType == null) {
                error(previous(), "'other' block only allowed for typed functions.")
            }
            consume(TokenType.LEFT_BRACE, "Expect '{' before 'other' block.")
            otherBlock = blockStmt()
        }

        return Decl.FunDecl(returnType, name, params, body, otherBlock)
    }

    // ---- Top-level var/const (returns Decl) ----

    // lagay IDENTIFIER [ = Expression ] ;
    private fun varDecl(): Decl.VarDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name after 'lagay'.")
        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Decl.VarDecl(name, initializer)
    }

    // peg IDENTIFIER [ = Expression ] ;
    private fun constDecl(): Decl.ConstDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect constant name after 'peg'.")
        var value: Expr? = null
        if (match(TokenType.EQUAL)) {
            value = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after constant declaration.")
        return Decl.ConstDecl(name, value)
    }

    // ---- Statement-level var/const (returns Stmt) ----

    // Called when 'lagay' token has already been consumed by match(...)
    private fun varStatement(): Stmt.VarDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name after 'lagay'.")
        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.VarDecl(name, initializer)
    }

    // Called when 'peg' token has already been consumed by match(...)
    private fun constStatement(): Stmt.VarDecl {
        // reuse Stmt.VarDecl shape for statement-level const (you might want a separate Stmt.ConstDecl)
        val name = consume(TokenType.IDENTIFIER, "Expect constant name after 'peg'.")
        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after constant declaration.")
        return Stmt.VarDecl(name, initializer)
    }

    // aray IDENTIFIER { ArayMember } ;
    private fun arayDecl(): Decl.ArayDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect array name after 'aray'.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before array members.")
        val members = mutableListOf<Decl.ArayMember>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            members.add(arayMember())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after array members.")
        consume(TokenType.SEMICOLON, "Expect ';' after aray declaration.")
        return Decl.ArayDecl(name, members)
    }

    private fun arayMember(): Decl.ArayMember {
        val type = consume(TokenType.IDENTIFIER, "Expect data type or 'aray' in aray member.")
        val name = consume(TokenType.IDENTIFIER, "Expect member name.")
        var size: Int? = null
        if (match(TokenType.LEFT_BRACKET)) {
            val numTok = consume(TokenType.NUMBER, "Expect array size.")
            size = numTok.lexeme.toInt()
            consume(TokenType.RIGHT_BRACKET, "Expect ']' after size.")
        }
        consume(TokenType.SEMICOLON, "Expect ';' after aray member.")
        return Decl.ArayMember(type, name, size)
    }

    // enum IDENTIFIER { EnumList } ;
    private fun enumDecl(): Decl.EnumDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect enum name after 'enum'.")
        consume(TokenType.LEFT_BRACE, "Expect '{' before enum members.")
        val entries = mutableListOf<Token>()
        do {
            entries.add(consume(TokenType.IDENTIFIER, "Expect enum member name."))
        } while (match(TokenType.COMMA))
        consume(TokenType.RIGHT_BRACE, "Expect '}' after enum members.")
        consume(TokenType.SEMICOLON, "Expect ';' after enum declaration.")
        return Decl.EnumDecl(name, entries)
    }

    // ---- Statements ----
    private fun statement(): Stmt {
        return when {
            match(TokenType.VAR) -> varStatement()
            match(TokenType.CONST) -> constStatement()
            match(TokenType.PRINT) -> printStmt()
            match(TokenType.IF_CONDITIONAL) -> ifStmt()
            match(TokenType.WHILE_LOOP) -> whileStmt()
            match(TokenType.FOR_LOOP) -> forStmt()
            match(TokenType.RETURN_CALL) -> returnStmt()
            match(TokenType.LEFT_BRACE) -> blockStmt()
            else -> exprStmt()
        }
    }

    private fun printStmt(): Stmt.PrintStmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'chika'.")
        val expr = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after value.")
        consume(TokenType.SEMICOLON, "Expect ';' after print statement.")
        return Stmt.PrintStmt(expr)
    }

    // kung (expr) stmt { kungdi (expr) stmt } [ kundi stmt ]
    private fun ifStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'kung'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()

        val elseIfBranches = mutableListOf<Pair<Expr, Stmt>>()
        while (match(TokenType.ELSE_IF_CONDITIONAL)) { // kungdi
            consume(TokenType.LEFT_PAREN, "Expect '(' after 'kungdi'.")
            val cond = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
            val branch = statement()
            elseIfBranches.add(cond to branch)
        }

        var elseBranch: Stmt? = null
        if (match(TokenType.ELSE_CONDITIONAL)) { // kundi
            elseBranch = statement()
        }

        return Stmt.IfStmt(condition, thenBranch, elseIfBranches, elseBranch)
    }

    // habang (expr) stmt
    private fun whileStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'habang'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Stmt.WhileStmt(condition, body)
    }

    // para ( init cond step ) stmt
    private fun forStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'para'.")

        // ForInit
        val initializer: Stmt? = when {
            match(TokenType.VAR) -> varStatement()
            match(TokenType.CONST) -> constStatement()
            !check(TokenType.SEMICOLON) -> exprStmt()
            else -> null
        }

        consume(TokenType.SEMICOLON, "Expect ';' after loop initializer.")

        // ForCond
        val condition: Expr? =
            if (!check(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        // ForStep
        val increment: Expr? =
            if (!check(TokenType.RIGHT_PAREN)) expression() else null
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        val body = statement()

        return Stmt.ForStmt(initializer, condition, increment, body)
    }

    private fun returnStmt(): Stmt.ReturnStmt {
        val keyword = previous()
        var value: Expr? = null
        if (!check(TokenType.SEMICOLON)) {
            value = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return Stmt.ReturnStmt(keyword, value)
    }

    private fun blockStmt(): Stmt.Block {
        val stmts = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(statement())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return Stmt.Block(stmts)
    }

    private fun exprStmt(): Stmt.ExpressionStmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.ExpressionStmt(expr)
    }

    // ---- Expressions ----
    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = logicOr()
        if (match(TokenType.EQUAL, TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL, TokenType.STAR_EQUAL, TokenType.DIVIDE_EQUAL, TokenType.MODULO_EQUAL)) {
            val operator = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Binary(Expr.Variable(name), operator, value)
            }
            error(operator, "Invalid assignment target.")
        }
        return expr
    }

    private fun logicOr(): Expr {
        var expr = logicAnd()
        while (match(TokenType.OR_OR, TokenType.OR_WORD)) {
            val operator = previous()
            val right = logicAnd()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun logicAnd(): Expr {
        var expr = equality()
        while (match(TokenType.AND_AND, TokenType.AND_WORD)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.STAR, TokenType.DIVIDE, TokenType.MODULO)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS, TokenType.NOT_WORD)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.NUMBER)) return Expr.Literal(previous().lexeme.toDouble())
        if (match(TokenType.STRING)) return Expr.Literal(previous().literal)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.NULL)) return Expr.Literal(null)
        if (match(TokenType.IDENTIFIER)) return Expr.Variable(previous())
        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
        error(peek(), "Expect expression.")
        return Expr.Literal(null)
    }

    // ---- Helpers ----
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        error(peek(), message)
        return Token(type, "", null, peek().line)
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF
    private fun peek(): Token = tokens[current]
    private fun previous(): Token = tokens[current - 1]

    private fun error(token: Token, message: String) {
        System.err.println("[line ${token.line}] Error at '${token.lexeme}': $message")
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return
            when (peek().type) {
                TokenType.FUNCTION_DECLARATION,
                TokenType.VAR,
                TokenType.CONST,
                TokenType.IF_CONDITIONAL,
                TokenType.WHILE_LOOP,
                TokenType.FOR_LOOP,
                TokenType.RETURN_CALL -> return
                else -> {}
            }
            advance()
        }
    }
}