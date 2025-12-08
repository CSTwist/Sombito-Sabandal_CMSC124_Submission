// Parser.kt
//
// Fully corrected parser consistent with:
// - Decl.kt
// - Expr.kt
// - Stmt.kt
// - Program.kt
// - Evaluator.kt
// - AstPrinter.kt
//
// Grammar is unchanged. All inconsistencies removed.
//

class Parser(private val tokens: List<Token>) {

    private var current = 0

    // ============================================================
    // PROGRAM ENTRY
    // ============================================================
    fun parseProgram(): Program {

        val imports = mutableListOf<Decl.ImportDecl>()

        while (match(TokenType.IMPORT)) {
            imports.add(importDecl())
        }

        consume(TokenType.GAME, "Expect 'GAME'.")
        val gameName = consume(TokenType.IDENTIFIER, "Expect game name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after game name.")

        val vars = mutableListOf<Decl.VarDecl>()
        val heroes = mutableListOf<Decl.HeroDecl>()
        val arenaItems = mutableListOf<Decl>()
        val teams = mutableListOf<Decl.TeamDecl>()
        val statusEffects = mutableListOf<Decl.StatusEffectDecl>()
        val items = mutableListOf<Decl.ItemDecl>()
        val creeps = mutableListOf<Decl.CreepDecl>()
        val functions = mutableListOf<Decl.FunctionDecl>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.SET) -> vars.add(setDecl())
                matchIdentifierAssignment() -> vars.add(assignmentDecl())
                match(TokenType.HEROES) -> parseHeroesBlock(heroes)
                match(TokenType.ARENA) -> parseArenaBlock(arenaItems, teams)
                match(TokenType.STATUS_EFFECTS) -> parseStatusEffectsBlock(statusEffects)
                match(TokenType.ITEMS) -> parseItemsBlock(items)
                match(TokenType.CREEPS) -> parseCreepsBlock(creeps)
                match(TokenType.FUNCTIONS) -> parseFunctionsBlock(functions)

                else -> {
                    error(peek(), "Unexpected token in game body.")
                    advance()
                }
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after game body.")

        return Program(
            gameName,
            imports,
            vars,
            heroes,
            arenaItems,
            teams,
            statusEffects,
            items,
            creeps,
            functions
        )
    }

    private fun matchIdentifierAssignment(): Boolean {
        if (!check(TokenType.IDENTIFIER)) return false
        val saved = current
        advance()
        if (check(TokenType.EQUAL)) {
            current = saved
            return true
        }
        current = saved
        return false
    }

    // ============================================================
    // IMPORT
    // ============================================================
    private fun importDecl(): Decl.ImportDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect module name after import.")
        consume(TokenType.SEMICOLON, "Expect ';' after import.")
        return Decl.ImportDecl(name)
    }

    // ============================================================
    // VARIABLES
    // ============================================================
    private fun setDecl(): Decl.VarDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        consume(TokenType.EQUAL, "Expect '='.")
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Decl.VarDecl(name, value)
    }

    private fun assignmentDecl(): Decl.VarDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        consume(TokenType.EQUAL, "Expect '='.")
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';'.")
        return Decl.VarDecl(name, value)
    }

    // ============================================================
    // FUNCTIONS BLOCK
    // ============================================================
    private fun parseFunctionsBlock(list: MutableList<Decl.FunctionDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after Functions.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.FUNCTION)) {
                list.add(functionDecl())
            } else {
                error(peek(), "Expect 'function'.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after Functions block.")
    }

    private fun functionDecl(): Decl.FunctionDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        consume(TokenType.LEFT_PAREN, "Expect '('.")

        val params = mutableListOf<Param>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                val type = parseTypeExpr()
                val paramName = consume(TokenType.IDENTIFIER, "Expect parameter name.")
                params.add(Param(type, paramName))
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')'.")

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
        return Token(TokenType.IDENTIFIER, "Any", "Any", peek().line)
    }

    // ============================================================
    // HEROES BLOCK
    // ============================================================
    private fun parseHeroesBlock(list: MutableList<Decl.HeroDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after Heroes.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.HERO)) {
                list.add(heroDecl())
            } else {
                error(peek(), "Expect 'hero'.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
    }

    private fun heroDecl(): Decl.HeroDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect hero name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after hero name.")

        val statements = mutableListOf<HeroStatement>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(heroStatement())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return Decl.HeroDecl(name, statements)
    }

    private fun heroStatement(): HeroStatement {
        return when {
            match(TokenType.SET) -> {
                val name = consume(TokenType.IDENTIFIER, "Expect stat name.")
                consume(TokenType.EQUAL, "Expect '='.")
                val expr = expression()
                consume(TokenType.SEMICOLON, "Expect ';'.")
                HeroStatement.SetStmt(name, expr)
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
                        error(peek(), "Expect ability.")
                        advance()
                    }
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}'.")
                HeroStatement.AbilitiesBlock(abilities)
            }

            else -> {
                error(peek(), "Invalid hero statement.")
                advance()
                HeroStatement.SetStmt(previous(), Expr.Literal(null))
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
                if (match(TokenType.LEFT_BRACE)) {
                    val body = blockBodyOnly()
                    AbilityField.BehaviorField(body, null)
                } else {
                    val expr = pipelineExpr()
                    AbilityField.BehaviorField(null, expr)
                }
            }

            else -> {
                error(peek(), "Unexpected ability field.")
                advance()
                AbilityField.TypeField(Token(TokenType.IDENTIFIER, "unknown", "unknown", peek().line))
            }
        }
    }

    // ============================================================
    // PIPELINE
    // ============================================================
    private fun pipelineExpr(): Expr {
        var expr: Expr = Expr.FunctionCallExpr(functionCall())

        while (match(TokenType.PIPE_GREATER)) {
            val op = previous()
            val right = Expr.FunctionCallExpr(functionCall())
            expr = Expr.Binary(expr, op, right)
        }
        return expr
    }

    // ============================================================
    // ARENA BLOCK
    // ============================================================
    private fun parseArenaBlock(items: MutableList<Decl>, teams: MutableList<Decl.TeamDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.TEAM) -> teams.add(teamDecl())
                match(TokenType.TURRET) -> items.add(turretDecl())
                match(TokenType.CORE) -> items.add(coreDecl())
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
                match(TokenType.CORE) -> coreRef = consume(TokenType.IDENTIFIER, "Expect core reference.")
                match(TokenType.TURRETS) -> {
                    consume(TokenType.COLON, "Expect ':'.")
                    consume(TokenType.LEFT_BRACE, "Expect '{'.")
                    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                        match(TokenType.TURRET)
                        turrets.add(turretDecl())
                    }
                    consume(TokenType.RIGHT_BRACE, "Expect '}'.")
                }

                else -> {
                    error(peek(), "Unexpected token in team.")
                    advance()
                }
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after team.")
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

    // ============================================================
    // STATUS EFFECT BLOCK
    // ============================================================
    private fun parseStatusEffectsBlock(list: MutableList<Decl.StatusEffectDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {

            if (match(TokenType.STATUS_EFFECT)) {
                list.add(statusEffectDecl())
            } else {
                error(peek(), "Expect statusEffect.")
                advance()
            }
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

                else -> {
                    error(peek(), "Unexpected status effect field.")
                    advance()
                }
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return Decl.StatusEffectDecl(name, fields)
    }

    // ============================================================
    // ITEMS BLOCK
    // ============================================================
    private fun parseItemsBlock(list: MutableList<Decl.ItemDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {

            if (match(TokenType.ITEM)) {
                list.add(itemDecl())
            } else {
                error(peek(), "Expect item.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
    }

    private fun itemDecl(): Decl.ItemDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect item name.")
        consume(TokenType.LEFT_BRACE, "Expect '{'.")

        val fields = mutableListOf<ItemField>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {

            if (match(TokenType.PASSIVE)) {
                consume(TokenType.COLON, "Expect ':'.")
                consume(TokenType.LEFT_BRACE, "Expect '{'.")
                if (match(TokenType.BEHAVIOR)) {
                    consume(TokenType.COLON, "Expect ':'.")
                    fields.add(ItemField.PassiveField(pipelineExpr()))
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}'.")
            }

            else if (check(TokenType.IDENTIFIER)) {
                val prop = advance()
                consume(TokenType.COLON, "Expect ':'.")
                fields.add(ItemField.PropertyField(prop, expression()))
            }

            else {
                error(peek(), "Unexpected field in item.")
                advance()
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return Decl.ItemDecl(name, fields)
    }

    // ============================================================
    // CREEPS
    // ============================================================
    private fun parseCreepsBlock(list: MutableList<Decl.CreepDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {

            if (match(TokenType.CREEP)) {
                val name = consume(TokenType.IDENTIFIER, "Expect creep name.")
                consume(TokenType.LEFT_BRACE, "Expect '{'.")
                val stats = mutableListOf<StatEntry>()
                while (!check(TokenType.RIGHT_BRACE)) {
                    stats.add(statEntry())
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}'.")
                list.add(Decl.CreepDecl(name, stats))
            }

            else {
                error(peek(), "Expect creep.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
    }

    // ============================================================
    // STAT ENTRY
    // ============================================================
    private fun statEntry(): StatEntry {
        val name = consume(TokenType.IDENTIFIER, "Expect stat name.")
        consume(TokenType.COLON, "Expect ':'.")
        return StatEntry(name, expression())
    }

    // ============================================================
    // BLOCK
    // ============================================================
    private fun block(): BlockStmt {
        consume(TokenType.LEFT_BRACE, "Expect '{'.")
        return blockBodyOnly()
    }

    private fun blockBodyOnly(): BlockStmt {
        val list = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            list.add(statement())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'.")
        return BlockStmt(list)
    }

    // ============================================================
    // STATEMENTS
    // ============================================================
    private fun statement(): Stmt {
        return when {
            match(TokenType.IF) -> ifStatement()
            match(TokenType.WHILE) -> whileStmt()
            match(TokenType.FOR) -> forStmt()
            match(TokenType.RETURN) -> returnStmt()
            match(TokenType.SET) -> setStatement()
            match(TokenType.CONST) -> constStatement()
            match(TokenType.APPLY) -> applyStatement()

            check(TokenType.IDENTIFIER) -> identifierStatement()

            else -> exprStatement()
        }
    }

    private fun setStatement(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable.")
        consume(TokenType.EQUAL, "Expect '='.")
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';'.")
        return Stmt.SetStmt(name, value)
    }

    private fun constStatement(): Stmt {
        val type = parseTypeExpr()
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        consume(TokenType.EQUAL, "Expect '='.")
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';'.")
        return Stmt.ConstDeclStmt(type, name, value)
    }

    private fun applyStatement(): Stmt {
        val call = functionCall()
        consume(TokenType.TO, "Expect 'to'.")
        val target = targetExpr()
        consume(TokenType.SEMICOLON, "Expect ';'.")
        return Stmt.ApplyStmt(call, target)
    }

    private fun identifierStatement(): Stmt {
        val saved = current
        val id = advance()

        return when {
            match(TokenType.EQUAL) -> {
                val value = expression()
                consume(TokenType.SEMICOLON, "Expect ';'.")
                Stmt.SetStmt(id, value)
            }

            match(TokenType.COLON) -> {
                val expr = expression()
                Stmt.StatEntryStmt(StatEntry(id, expr))
            }

            match(TokenType.LEFT_PAREN) -> {
                current = saved
                Stmt.ExprStmt(expression())
            }

            else -> {
                current = saved
                Stmt.ExprStmt(expression())
            }
        }
    }

    private fun exprStatement(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';'.")
        return Stmt.ExprStmt(expr)
    }

    private fun returnStmt(): Stmt {
        val keyword = previous()
        val value = if (check(TokenType.SEMICOLON)) null else expression()
        consume(TokenType.SEMICOLON, "Expect ';'.")
        return Stmt.ReturnStmt(keyword, value)
    }

    private fun ifStatement(): Stmt.IfStmt {
        consume(TokenType.LEFT_PAREN, "Expect '('.")
        val cond = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')'.")
        val thenBranch = block()
        val elseBranch =
            if (match(TokenType.ELSE)) block() else null
        return Stmt.IfStmt(cond, thenBranch, elseBranch)
    }

    private fun whileStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '('.")
        val cond = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')'.")
        val body = block()
        return Stmt.WhileStmt(cond, body)
    }

    private fun forStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '('.")
        val name = consume(TokenType.IDENTIFIER, "Expect loop variable.")
        consume(TokenType.IN, "Expect 'in'.")
        val collection = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')'.")
        val body = block()
        return Stmt.ForStmt(name, collection, body)
    }

    // ============================================================
    // EXPRESSION PARSER
    // ============================================================
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
        return if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            Expr.Unary(op, right)
        } else {
            call()
        }
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
        return when {
            match(TokenType.NUMBER) ->
                Expr.Literal(previous().lexeme.toDouble())

            match(TokenType.STRING) ->
                Expr.Literal(previous().literal)

            match(TokenType.PERCENTAGE) -> {
                val raw = previous().lexeme.removeSuffix("%")
                Expr.Percentage(raw.toDouble())
            }

            match(TokenType.TIME) -> {
                val raw = previous().lexeme.removeSuffix("s")
                Expr.Time(raw.toInt())
            }

            match(TokenType.IDENTIFIER) ->
                Expr.Variable(previous())

            match(TokenType.LEFT_PAREN) -> {
                val expr = expression()
                consume(TokenType.RIGHT_PAREN, "Expect ')'.")
                Expr.Grouping(expr)
            }

            else -> {
                error(peek(), "Expect expression.")
                Expr.Literal(null)
            }
        }
    }

    // ============================================================
    // FUNCTION CALL ARGUMENT PARSER
    // ============================================================
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
                        continue
                    }
                    current = saved
                }
                args.add(Argument.PositionalArg(expression()))
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
            else -> {
                error(peek(), "Invalid target.")
                TargetExpr.Self
            }
        }
    }

    // ============================================================
    // INTERNAL HELPERS
    // ============================================================
    private fun match(vararg types: TokenType): Boolean {
        for (t in types) {
            if (check(t)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType) =
        !isAtEnd() && peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean =
        peek().type == TokenType.EOF

    private fun peek(): Token =
        tokens[current]

    private fun previous(): Token =
        tokens[current - 1]

    private fun consume(type: TokenType, msg: String): Token {
        if (check(type)) return advance()
        error(peek(), msg)
        return Token(type, "error", null, peek().line)
    }

    private fun error(token: Token, msg: String) {
        System.err.println("[Parser error line ${token.line}] at '${token.lexeme}': $msg")
    }
}
