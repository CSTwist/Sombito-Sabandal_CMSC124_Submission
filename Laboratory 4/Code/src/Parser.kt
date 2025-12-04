// Parser.kt
class Parser(private val tokens: List<Token>) {

    private var current = 0

    // ---- Entry Point: [imports] "GAME" IDENT { game_body } ----
    fun parseProgram(): Program {

        // 1. Imports
        val imports = mutableListOf<Decl.ImportDecl>()
        while (match(TokenType.IMPORT)) {
            imports.add(importDecl())
        }

        // 2. Game Header
        consume(TokenType.GAME, "Expect 'GAME' keyword.")
        val gameName = consume(TokenType.IDENTIFIER, "Expect game name identifier.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after game name.")

        // 3. Game Body
        val heroes = mutableListOf<Decl.HeroDecl>()
        val arenaItems = mutableListOf<Decl>()
        val teams = mutableListOf<Decl.TeamDecl>()
        val statusEffects = mutableListOf<Decl.StatusEffectDecl>()
        val items = mutableListOf<Decl.ItemDecl>()
        val creeps = mutableListOf<Decl.CreepDecl>()
        val variables = mutableListOf<Decl.VarDecl>()
        val functions = mutableListOf<Decl.FunctionDecl>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.SET) -> variables.add(setDecl())
                match(TokenType.HEROES) -> parseHeroesBlock(heroes)
                match(TokenType.ARENA) -> parseArenaBlock(arenaItems, teams)
                match(TokenType.STATUS_EFFECTS) -> parseStatusEffectsBlock(statusEffects)
                match(TokenType.ITEMS) -> parseItemsBlock(items)
                match(TokenType.CREEPS) -> parseCreepsBlock(creeps)
                match(TokenType.FUNCTIONS) -> parseFunctionsBlock(functions)
                else -> {
                    error(peek(), "Unexpected token in game body: '${peek().lexeme}'")
                    advance()
                }
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after game body.")
        return Program(gameName, imports, variables, heroes, arenaItems, teams, statusEffects, items, creeps, functions)
    }

    // ---- Import: import IDENTIFIER ; ----
    private fun importDecl(): Decl.ImportDecl {
        // 'import' already consumed
        val name = consume(TokenType.IDENTIFIER, "Expect identifier after 'import'.")
        consume(TokenType.SEMICOLON, "Expect ';' after import statement.")
        return Decl.ImportDecl(name)
    }

    // ---- Set: set IDENT = EXPR ; ----
    private fun setDecl(): Decl.VarDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect identifier after 'set'.")
        consume(TokenType.EQUAL, "Expect '=' after identifier.")
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Decl.VarDecl(name, value)
    }

    // ---- Functions Block ----
    private fun parseFunctionsBlock(functions: MutableList<Decl.FunctionDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'Functions'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.FUNCTION)) {
                functions.add(functionDecl())
            } else {
                error(peek(), "Expect 'function' in Functions block.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after Functions block.")
    }

    private fun functionDecl(): Decl.FunctionDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")

        val params = mutableListOf<Param>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                val type = parseTypeExpr()
                val paramName = consume(TokenType.IDENTIFIER, "Expect parameter name.")
                params.add(Param(type, paramName))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after params.")

        var returnType: Token? = null
        if (match(TokenType.COLON)) {
            returnType = parseTypeExpr()
        }

        val body = block()
        return Decl.FunctionDecl(name, params, returnType, body)
    }

    private fun parseTypeExpr(): Token {
        if (match(TokenType.IDENTIFIER)) return previous()
        error(peek(), "Expect type name.")
        return Token(TokenType.INVALID, "", null, 0)
    }

    // ---- Heroes Block ----
    private fun parseHeroesBlock(heroes: MutableList<Decl.HeroDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'Heroes'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.HERO)) {
                heroes.add(heroDecl())
            } else {
                error(peek(), "Expect 'hero' declaration.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after Heroes block.")
    }

    private fun heroDecl(): Decl.HeroDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect hero name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after hero name.")
        val statements = mutableListOf<HeroStatement>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(heroStatement())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after hero body.")
        return Decl.HeroDecl(name, statements)
    }

    private fun heroStatement(): HeroStatement {
        return when {
            match(TokenType.SET) -> {
                val name = consume(TokenType.IDENTIFIER, "Expect identifier.")
                consume(TokenType.EQUAL, "Expect '='.")
                val value = expression()
                consume(TokenType.SEMICOLON, "Expect ';' after set.")
                HeroStatement.SetStmt(name, value)
            }
            match(TokenType.HERO_STAT) -> {
                consume(TokenType.COLON, "Expect ':'.")
                consume(TokenType.LEFT_BRACE, "Expect '{'.")
                val stats = mutableListOf<StatEntry>()
                while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                    stats.add(statEntry())
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}'.")
                HeroStatement.HeroStatBlock(stats)
            }
            match(TokenType.ABILITIES) -> {
                consume(TokenType.COLON, "Expect ':'.")
                consume(TokenType.LEFT_BRACE, "Expect '{'.")
                val abilities = mutableListOf<AbilityDecl>()
                while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                    if (match(TokenType.ABILITY)) {
                        abilities.add(abilityDecl())
                    } else {
                        error(peek(), "Expect 'ability' definition.")
                        advance()
                    }
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}'.")
                HeroStatement.AbilitiesBlock(abilities)
            }
            else -> {
                error(peek(), "Unexpected token in hero body.")
                advance()
                HeroStatement.SetStmt(Token(TokenType.INVALID, "", null, 0), Expr.Literal(null))
            }
        }
    }

    private fun abilityDecl(): AbilityDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect ability name.")
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        val fields = mutableListOf<AbilityField>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            fields.add(abilityField())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return AbilityDecl(name, fields)
    }

    private fun abilityField(): AbilityField {
        return when {
            match(TokenType.TYPE) -> {
                consume(TokenType.COLON, "Expect ':'.")
                AbilityField.TypeField(consume(TokenType.IDENTIFIER, "Expect type."))
            }
            match(TokenType.COOLDOWN) -> {
                consume(TokenType.COLON, "Expect ':'.")
                AbilityField.CooldownField(expression())
            }
            match(TokenType.MANA_COST) -> {
                consume(TokenType.COLON, "Expect ':'.")
                AbilityField.ManaCostField(expression())
            }
            match(TokenType.RANGE) -> {
                consume(TokenType.COLON, "Expect ':'.")
                AbilityField.RangeField(expression())
            }
            match(TokenType.DAMAGE_TYPE) -> {
                consume(TokenType.COLON, "Expect ':'.")
                AbilityField.DamageTypeField(consume(TokenType.IDENTIFIER, "Expect damage type."))
            }
            match(TokenType.BEHAVIOR) -> {
                consume(TokenType.COLON, "Expect ':'.")
                if (check(TokenType.LEFT_BRACE)) {
                    AbilityField.BehaviorField(block(), null)
                } else {
                    AbilityField.BehaviorField(null, pipelineExpr())
                }
            }
            else -> {
                error(peek(), "Unexpected in ability.")
                advance()
                AbilityField.TypeField(Token(TokenType.INVALID, "", null, 0))
            }
        }
    }

    // ---- Pipeline Expression: func() |> func() ----
    private fun pipelineExpr(): Expr {
        var expr: Expr = Expr.FunctionCallExpr(functionCall())

        while (match(TokenType.PIPE_GREATER)) {
            val op = previous()
            val rightCall = functionCall()
            val rightExpr = Expr.FunctionCallExpr(rightCall)
            expr = Expr.Binary(expr, op, rightExpr)
        }
        return expr
    }

    // ---- Arena Block ----
    private fun parseArenaBlock(arenaItems: MutableList<Decl>, teams: MutableList<Decl.TeamDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.TEAM) -> teams.add(teamDecl())
                match(TokenType.TURRET) -> arenaItems.add(turretDecl())
                match(TokenType.CORE) -> arenaItems.add(coreDecl())
                else -> {
                    error(peek(), "Unexpected in Arena.")
                    advance()
                }
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
    }

    private fun teamDecl(): Decl.TeamDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect team name.")
        consume(TokenType.LEFT_BRACE, "Expect '{'.")

        var coreRef: Token? = null
        val turrets = mutableListOf<Decl.TurretDecl>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.CORE) -> {
                    coreRef = consume(TokenType.IDENTIFIER, "Expect core name ref.")
                }
                match(TokenType.TURRETS) -> {
                    consume(TokenType.COLON, "Expect ':'.")
                    consume(TokenType.LEFT_BRACE, "Expect '{'.")
                    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                        if (match(TokenType.TURRET)) {
                            turrets.add(turretDecl())
                        } else {
                            error(peek(), "Expect 'turret' definition.")
                            advance()
                        }
                    }
                    consume(TokenType.RIGHT_BRACE, "Expect '}' after turrets.")
                }
                else -> {
                    error(peek(), "Unexpected inside Team.")
                    advance()
                }
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after Team.")
        return Decl.TeamDecl(name, coreRef, turrets)
    }

    private fun turretDecl(): Decl.TurretDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect turret name.")
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        val stats = mutableListOf<StatEntry>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stats.add(statEntry())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return Decl.TurretDecl(name, stats)
    }

    private fun coreDecl(): Decl.CoreDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect core name.")
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        val stats = mutableListOf<StatEntry>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stats.add(statEntry())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return Decl.CoreDecl(name, stats)
    }

    // ---- Status Effects ----
    private fun parseStatusEffectsBlock(effects: MutableList<Decl.StatusEffectDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.STATUS_EFFECT)) effects.add(statusEffectDecl())
            else { error(peek(), "Expect statusEffect."); advance() }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
    }

    private fun statusEffectDecl(): Decl.StatusEffectDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect name.")
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        val fields = mutableListOf<StatusEffectField>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.TYPE) -> {
                    consume(TokenType.COLON, "Expect ':'.")
                    fields.add(StatusEffectField.TypeField(consume(TokenType.IDENTIFIER, "Expect type.")))
                }
                match(TokenType.DURATION) -> {
                    consume(TokenType.COLON, "Expect ':'.")
                    fields.add(StatusEffectField.DurationField(expression()))
                }
                match(TokenType.ON_APPLY) -> {
                    consume(TokenType.COLON, "Expect ':'.")
                    fields.add(StatusEffectField.OnApplyField(block()))
                }
                match(TokenType.ON_TICK) -> {
                    consume(TokenType.COLON, "Expect ':'.")
                    fields.add(StatusEffectField.OnTickField(block()))
                }
                match(TokenType.ON_EXPIRE) -> {
                    consume(TokenType.COLON, "Expect ':'.")
                    fields.add(StatusEffectField.OnExpireField(block()))
                }
                else -> { error(peek(), "Unexpected field."); advance() }
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return Decl.StatusEffectDecl(name, fields)
    }

    // ---- Items ----
    private fun parseItemsBlock(items: MutableList<Decl.ItemDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.ITEM)) items.add(itemDecl())
            else { error(peek(), "Expect item."); advance() }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
    }

    private fun itemDecl(): Decl.ItemDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect name.")
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        val fields = mutableListOf<ItemField>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.PASSIVE)) {
                consume(TokenType.COLON, "Expect ':'.")
                consume(TokenType.LEFT_BRACE, "Expect '{'.")
                if (match(TokenType.BEHAVIOR)) {
                    consume(TokenType.COLON, "Expect ':'.")
                    val expr = pipelineExpr()
                    fields.add(ItemField.PassiveField(expr))
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}'.")
            } else if (check(TokenType.IDENTIFIER)) {
                val propName = advance()
                consume(TokenType.COLON, "Expect ':'.")
                fields.add(ItemField.PropertyField(propName, expression()))
            } else {
                error(peek(), "Unexpected in item.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return Decl.ItemDecl(name, fields)
    }

    // ---- Creeps ----
    private fun parseCreepsBlock(creeps: MutableList<Decl.CreepDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.CREEP)) {
                val name = consume(TokenType.IDENTIFIER, "Expect name.")
                consume(TokenType.LEFT_BRACE, "Expect '{'.")
                val stats = mutableListOf<StatEntry>()
                while(!check(TokenType.RIGHT_BRACE)) stats.add(statEntry())
                consume(TokenType.RIGHT_BRACE, "Expect '}'.")
                creeps.add(Decl.CreepDecl(name, stats))
            } else {
                error(peek(), "Expect creep."); advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
    }

    // ---- Statements ----
    private fun block(): BlockStmt {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        val stmts = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(statement())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return BlockStmt(stmts)
    }

    private fun statement(): Stmt {
        if (match(TokenType.IF)) return ifStatement()
        if (match(TokenType.WHILE)) return whileStatement()
        if (match(TokenType.FOR)) return forStatement()

        if (match(TokenType.RETURN)) {
            val keyword = previous()
            val value = if (check(TokenType.SEMICOLON)) null else expression()
            consume(TokenType.SEMICOLON, "Expect ';'.")
            return Stmt.ReturnStmt(keyword, value)
        }

        if (match(TokenType.CONST)) {
            val type = parseTypeExpr()
            val name = consume(TokenType.IDENTIFIER, "Expect name.")
            consume(TokenType.EQUAL, "Expect '='.")
            val value = expression()
            consume(TokenType.SEMICOLON, "Expect ';'.")
            return Stmt.ConstDeclStmt(type, name, value)
        }

        if (match(TokenType.SET)) {
            val name = consume(TokenType.IDENTIFIER, "Expect name.")
            consume(TokenType.EQUAL, "Expect '='.")
            val value = expression()
            consume(TokenType.SEMICOLON, "Expect ';'.")
            return Stmt.SetStmt(name, value)
        }

        if (match(TokenType.APPLY)) {
            val call = functionCall()
            consume(TokenType.TO, "Expect 'to'.")
            val target = targetExpr()
            consume(TokenType.SEMICOLON, "Expect ';'.")
            return Stmt.ApplyStmt(call, target)
        }

        // Check for Identifiers (Assignment, StatEntry, or Function Calls)
        if (check(TokenType.IDENTIFIER)) {
            val saved = current
            val name = advance()

            if (match(TokenType.EQUAL)) {
                // Reassignment: name = value; (No 'set' required)
                val value = expression()
                consume(TokenType.SEMICOLON, "Expect ';' after assignment.")
                return Stmt.SetStmt(name, value)
            }
            else if (match(TokenType.COLON)) {
                // Stat entry: name : value
                val value = expression()
                return Stmt.StatEntryStmt(StatEntry(name, value))
            }

            // Backtrack if not assignment or stat entry
            current = saved
        }

        // Expression statement (e.g., function call)
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.ExprStmt(expr)
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '('.")
        val cond = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')'.")
        val thenBranch = block()
        var elseBranch: BlockStmt? = null
        if (match(TokenType.ELSE)) {
            elseBranch = if (check(TokenType.IF)) BlockStmt(listOf(ifStatement())) else block()
        }
        return Stmt.IfStmt(cond, thenBranch, elseBranch)
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '('.")
        val cond = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')'.")
        return Stmt.WhileStmt(cond, block())
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '('.")
        val name = consume(TokenType.IDENTIFIER, "Expect loop variable.")
        consume(TokenType.IN, "Expect 'in'.")
        val collection = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')'.")
        return Stmt.ForStmt(name, collection, block())
    }

    // ---- Expression logic ----
    fun parseExpression(): Expr {
        return expression()
    }

    private fun expression(): Expr = or()
    private fun or(): Expr {
        var expr = and()
        while (match(TokenType.OR)) {
            val op = previous()
            val right = and()
            expr = Expr.Logical(expr, op, right)
        }
        return expr
    }
    private fun and(): Expr {
        var expr = equality()
        while (match(TokenType.AND)) {
            val op = previous()
            val right = equality()
            expr = Expr.Logical(expr, op, right)
        }
        return expr
    }
    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }
    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val op = previous()
            val right = term()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }
    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous()
            val right = factor()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }
    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.STAR, TokenType.DIVIDE)) {
            val op = previous()
            val right = unary()
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }
    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            return Expr.Unary(op, right)
        }
        return call()
    }
    private fun call(): Expr {
        if (check(TokenType.IDENTIFIER)) {
            val saved = current
            advance()
            if (check(TokenType.LEFT_PAREN)) {
                current = saved
                return Expr.FunctionCallExpr(functionCall())
            }
            current = saved
        }
        return primary()
    }
    private fun primary(): Expr {
        if (match(TokenType.NUMBER)) return Expr.Literal(previous().lexeme.toDouble())
        if (match(TokenType.STRING)) return Expr.Literal(previous().literal)
        if (match(TokenType.PERCENTAGE)) return Expr.Percentage(previous().lexeme.removeSuffix("%").toDouble())
        if (match(TokenType.TIME)) return Expr.Time(previous().lexeme.removeSuffix("s").toInt())
        if (match(TokenType.IDENTIFIER)) return Expr.Variable(previous())
        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')'.")
            return Expr.Grouping(expr)
        }
        error(peek(), "Expect expression.")
        return Expr.Literal(null)
    }

    // ---- Helpers ----
    private fun statEntry(): StatEntry {
        val name = consume(TokenType.IDENTIFIER, "Expect stat name.")
        consume(TokenType.COLON, "Expect ':'.")
        val value = expression()
        return StatEntry(name, value)
    }

    private fun functionCall(): FunctionCall {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        consume(TokenType.LEFT_PAREN, "Expect '('.")
        val args = mutableListOf<Argument>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (check(TokenType.IDENTIFIER)) {
                    val saved = current
                    val id = advance()
                    if (match(TokenType.COLON)) {
                        args.add(Argument.NamedArg(id, expression()))
                    } else {
                        current = saved
                        args.add(Argument.PositionalArg(expression()))
                    }
                } else {
                    args.add(Argument.PositionalArg(expression()))
                }
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')'.")
        return FunctionCall(name, args)
    }

    private fun targetExpr(): TargetExpr {
        return when {
            match(TokenType.SELF) -> TargetExpr.Self
            match(TokenType.TARGET) -> TargetExpr.Target
            match(TokenType.CASTER) -> TargetExpr.Caster
            match(TokenType.IDENTIFIER) -> TargetExpr.Named(previous())
            else -> { error(peek(), "Expect target."); TargetExpr.Self }
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        for (t in types) if (check(t)) { advance(); return true }
        return false
    }
    private fun check(type: TokenType) = !isAtEnd() && peek().type == type
    private fun advance(): Token { if (!isAtEnd()) current++; return previous() }
    private fun isAtEnd() = peek().type == TokenType.EOF
    private fun peek() = tokens[current]
    private fun previous() = tokens[current - 1]
    private fun consume(type: TokenType, msg: String): Token {
        if (check(type)) return advance()
        error(peek(), msg)
        return Token(type, "", null, peek().line)
    }
    private fun error(token: Token, msg: String) {
        System.err.println("[line ${token.line}] Error at '${token.lexeme}': $msg")
    }
}