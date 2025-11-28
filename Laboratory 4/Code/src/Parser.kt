// Parser.kt
class Parser(private val tokens: List<Token>) {

    private var current = 0

    // ---- Entry Point: program = "GAME" "{" game_body "}" ----
    // Parser.kt - Updated parseProgram() method
    fun parseProgram(): Program {
        consume(TokenType.GAME, "Expect 'GAME' at start of program.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'GAME'.")

        val imports = mutableListOf<Decl.ImportDecl>()
        val heroes = mutableListOf<Decl.HeroDecl>()
        val arenaItems = mutableListOf<Decl>()
        val statusEffects = mutableListOf<Decl.StatusEffectDecl>()
        val items = mutableListOf<Decl.ItemDecl>()
        val creeps = mutableListOf<Decl.CreepDecl>()
        val variables = mutableListOf<Decl.VarDecl>()  // NEW: Store set statements

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.IMPORT) -> imports.add(importDecl())
                match(TokenType.SET) -> variables.add(setDecl())  // NEW: Handle set at top level
                match(TokenType.HEROES) -> parseHeroesBlock(heroes)
                match(TokenType.ARENA) -> parseArenaBlock(arenaItems)
                match(TokenType.STATUS_EFFECTS) -> parseStatusEffectsBlock(statusEffects)
                match(TokenType.ITEMS) -> parseItemsBlock(items)
                match(TokenType.CREEPS) -> parseCreepsBlock(creeps)
                else -> {
                    error(peek(), "Unexpected token in game body: '${peek().lexeme}'")
                    advance()
                }
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after game body.")
        return Program(imports, variables, heroes, arenaItems, statusEffects, items, creeps)
    }

    // NEW: Parse set declarations at top level
    private fun setDecl(): Decl.VarDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect identifier after 'set'.")
        consume(TokenType.EQUAL, "Expect '=' after identifier.")
        val value = expression()
        return Decl.VarDecl(name, value)
    }

    // ---- Import: import IDENTIFIER ; ----
    private fun importDecl(): Decl.ImportDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect identifier after 'import'.")
        consume(TokenType.SEMICOLON, "Expect ';' after import statement.")
        return Decl.ImportDecl(name)
    }

    // ---- Heroes Block: Heroes { { hero_decl } } ----
    private fun parseHeroesBlock(heroes: MutableList<Decl.HeroDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'Heroes'.")
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.HERO)) {
                heroes.add(heroDecl())
            } else {
                error(peek(), "Expect 'hero' declaration in Heroes block.")
                advance()
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after Heroes block.")
    }

    // ---- Hero Declaration: hero IDENTIFIER { hero_body } ----
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

    // ---- Hero Statement ----
    private fun heroStatement(): HeroStatement {
        return when {
            match(TokenType.SET) -> {
                val name = consume(TokenType.IDENTIFIER, "Expect identifier after 'set'.")
                consume(TokenType.EQUAL, "Expect '=' after identifier.")
                val value = expression()
                HeroStatement.SetStmt(name, value)
            }
            match(TokenType.HERO_STAT) -> {
                consume(TokenType.COLON, "Expect ':' after 'heroStat'.")
                consume(TokenType.LEFT_BRACE, "Expect '{' after ':'.")
                val stats = mutableListOf<StatEntry>()
                while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                    stats.add(statEntry())
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}' after stat entries.")
                HeroStatement.HeroStatBlock(stats)
            }
            match(TokenType.SCALING) -> {
                consume(TokenType.LEFT_PAREN, "Expect '(' after 'scaling'.")
                val param1 = consume(TokenType.IDENTIFIER, "Expect first parameter.")
                consume(TokenType.COMMA, "Expect ',' between parameters.")
                val param2 = consume(TokenType.IDENTIFIER, "Expect second parameter.")
                consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
                HeroStatement.ScalingCall(param1, param2)
            }
            match(TokenType.ABILITIES) -> {
                consume(TokenType.COLON, "Expect ':' after 'abilities'.")
                consume(TokenType.LEFT_BRACE, "Expect '{' after ':'.")
                val abilities = mutableListOf<AbilityDecl>()
                while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                    if (match(TokenType.ABILITY)) {
                        abilities.add(abilityDecl())
                    } else {
                        error(peek(), "Expect 'ability' in abilities block.")
                        advance()
                    }
                }
                consume(TokenType.RIGHT_BRACE, "Expect '}' after abilities.")
                HeroStatement.AbilitiesBlock(abilities)
            }
            else -> {
                error(peek(), "Unexpected token in hero body: '${peek().lexeme}'")
                advance()
                HeroStatement.SetStmt(
                    Token(TokenType.IDENTIFIER, "error", null, peek().line),
                    Expr.Literal(null)
                )
            }
        }
    }

    // ---- Stat Entry: IDENTIFIER : expression ----
    private fun statEntry(): StatEntry {
        val name = consume(TokenType.IDENTIFIER, "Expect stat name.")
        consume(TokenType.COLON, "Expect ':' after stat name.")
        val value = expression()
        return StatEntry(name, value)
    }

    // ---- Ability Declaration ----
    private fun abilityDecl(): AbilityDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect ability name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after ability name.")

        val fields = mutableListOf<AbilityField>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            fields.add(abilityField())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after ability body.")
        return AbilityDecl(name, fields)
    }

    // ---- Ability Field ----
    private fun abilityField(): AbilityField {
        return when {
            match(TokenType.TYPE) -> {
                consume(TokenType.COLON, "Expect ':' after 'type'.")
                val value = consume(TokenType.IDENTIFIER, "Expect type identifier.")
                AbilityField.TypeField(value)
            }
            match(TokenType.COOLDOWN) -> {
                consume(TokenType.COLON, "Expect ':' after 'cooldown'.")
                AbilityField.CooldownField(expression())
            }
            match(TokenType.MANA_COST) -> {
                consume(TokenType.COLON, "Expect ':' after 'mana_cost'.")
                AbilityField.ManaCostField(expression())
            }
            match(TokenType.RANGE) -> {
                consume(TokenType.COLON, "Expect ':' after 'range'.")
                AbilityField.RangeField(expression())
            }
            match(TokenType.DAMAGE_TYPE) -> {
                consume(TokenType.COLON, "Expect ':' after 'damage_type'.")
                val value = consume(TokenType.IDENTIFIER, "Expect damage type identifier.")
                AbilityField.DamageTypeField(value)
            }
            match(TokenType.BEHAVIOR) -> {
                consume(TokenType.COLON, "Expect ':' after 'behavior'.")
                AbilityField.BehaviorField(pipelineExpr())
            }
            else -> {
                error(peek(), "Unexpected token in ability body: '${peek().lexeme}'")
                advance()
                AbilityField.TypeField(Token(TokenType.IDENTIFIER, "error", null, peek().line))
            }
        }
    }

    // ---- Pipeline Expression: function_call { |> function_call } ----
    private fun pipelineExpr(): PipelineExpr {
        val calls = mutableListOf<FunctionCall>()
        calls.add(functionCall())

        while (match(TokenType.PIPE_GREATER)) {
            calls.add(functionCall())
        }

        return PipelineExpr(calls)
    }

    // ---- Function Call: IDENTIFIER ( [ argument_list ] ) ----
    private fun functionCall(): FunctionCall {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.")

        val arguments = mutableListOf<Argument>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(argument())
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
        return FunctionCall(name, arguments)
    }

    // ---- Argument: IDENTIFIER : expression | expression ----
    private fun argument(): Argument {
        // Try to parse as named argument (IDENTIFIER : expression)
        if (check(TokenType.IDENTIFIER)) {
            val saved = current
            val name = advance()
            if (match(TokenType.COLON)) {
                val value = expression()
                return Argument.NamedArg(name, value)
            } else {
                // Backtrack - it's a positional argument
                current = saved
            }
        }
        return Argument.PositionalArg(expression())
    }

    // ---- Arena Block ----
    private fun parseArenaBlock(arenaItems: MutableList<Decl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'Arena'.")

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            when {
                match(TokenType.CONST) -> {
                    if (match(TokenType.TEAM)) {
                        val name = consume(TokenType.IDENTIFIER, "Expect team name.")
                        arenaItems.add(Decl.TeamDecl(name))
                    } else {
                        error(peek(), "Expect 'team' after 'const' in Arena block.")
                        advance()
                    }
                }
                match(TokenType.TURRET) -> {
                    val name = consume(TokenType.IDENTIFIER, "Expect turret name.")
                    consume(TokenType.LEFT_BRACE, "Expect '{' after turret name.")
                    val stats = mutableListOf<StatEntry>()
                    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                        stats.add(statEntry())
                    }
                    consume(TokenType.RIGHT_BRACE, "Expect '}' after turret stats.")
                    arenaItems.add(Decl.TurretDecl(name, stats))
                }
                match(TokenType.CORE) -> {
                    val name = consume(TokenType.IDENTIFIER, "Expect core name.")
                    consume(TokenType.LEFT_BRACE, "Expect '{' after core name.")
                    val stats = mutableListOf<StatEntry>()
                    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                        stats.add(statEntry())
                    }
                    consume(TokenType.RIGHT_BRACE, "Expect '}' after core stats.")
                    arenaItems.add(Decl.CoreDecl(name, stats))
                }
                else -> {
                    error(peek(), "Unexpected token in Arena block.")
                    advance()
                }
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after Arena block.")
    }

    // ---- Status Effects Block ----
    private fun parseStatusEffectsBlock(statusEffects: MutableList<Decl.StatusEffectDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'StatusEffects'.")

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.STATUS_EFFECT)) {
                statusEffects.add(statusEffectDecl())
            } else {
                error(peek(), "Expect 'statusEffect' in StatusEffects block.")
                advance()
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after StatusEffects block.")
    }

    private fun statusEffectDecl(): Decl.StatusEffectDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect status effect name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after status effect name.")

        val fields = mutableListOf<StatusEffectField>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            fields.add(statusEffectField())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after status effect body.")
        return Decl.StatusEffectDecl(name, fields)
    }

    private fun statusEffectField(): StatusEffectField {
        return when {
            match(TokenType.TYPE) -> {
                consume(TokenType.COLON, "Expect ':' after 'type'.")
                val value = consume(TokenType.IDENTIFIER, "Expect type identifier.")
                StatusEffectField.TypeField(value)
            }
            match(TokenType.DURATION) -> {
                consume(TokenType.COLON, "Expect ':' after 'duration'.")
                StatusEffectField.DurationField(expression())
            }
            match(TokenType.ON_APPLY) -> {
                consume(TokenType.COLON, "Expect ':' after 'on_apply'.")
                StatusEffectField.OnApplyField(block())
            }
            match(TokenType.ON_TICK) -> {
                consume(TokenType.COLON, "Expect ':' after 'on_tick'.")
                StatusEffectField.OnTickField(block())
            }
            else -> {
                error(peek(), "Unexpected token in status effect body.")
                advance()
                StatusEffectField.TypeField(Token(TokenType.IDENTIFIER, "error", null, peek().line))
            }
        }
    }

    // ---- Block: { { statement } } ----
    private fun block(): BlockStmt {
        consume(TokenType.LEFT_BRACE, "Expect '{' for block.")
        val statements = mutableListOf<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return BlockStmt(statements)
    }

    // ---- Statement ----
    private fun statement(): Stmt {
        return when {
            match(TokenType.APPLY) -> {
                val call = functionCall()
                consume(TokenType.TO, "Expect 'to' after function call.")
                val target = targetExpr()
                Stmt.ApplyStmt(call, target)
            }
            match(TokenType.SET) -> {
                val name = consume(TokenType.IDENTIFIER, "Expect identifier after 'set'.")
                consume(TokenType.EQUAL, "Expect '=' after identifier.")
                val value = expression()
                Stmt.SetStmt(name, value)
            }
            check(TokenType.IDENTIFIER) -> {
                // Could be stat entry, function call, or pipeline
                val saved = current
                val name = advance()
                if (match(TokenType.COLON)) {
                    // It's a stat entry
                    val value = expression()
                    Stmt.StatEntryStmt(StatEntry(name, value))
                } else if (match(TokenType.LEFT_PAREN)) {
                    // It's a function call - rebuild
                    current = saved
                    val call = functionCall()
                    Stmt.FunctionCallStmt(call)
                } else {
                    error(peek(), "Unexpected statement.")
                    Stmt.SetStmt(name, Expr.Literal(null))
                }
            }
            else -> {
                error(peek(), "Unexpected statement.")
                advance()
                Stmt.SetStmt(
                    Token(TokenType.IDENTIFIER, "error", null, peek().line),
                    Expr.Literal(null)
                )
            }
        }
    }

    // ---- Target Expression ----
    private fun targetExpr(): TargetExpr {
        return when {
            match(TokenType.SELF) -> TargetExpr.Self
            match(TokenType.TARGET) -> TargetExpr.Target
            match(TokenType.CASTER) -> TargetExpr.Caster
            match(TokenType.IDENTIFIER) -> TargetExpr.Named(previous())
            else -> {
                error(peek(), "Expect target expression.")
                TargetExpr.Self
            }
        }
    }

    // ---- Items Block ----
    private fun parseItemsBlock(items: MutableList<Decl.ItemDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'Items'.")

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.ITEM)) {
                items.add(itemDecl())
            } else {
                error(peek(), "Expect 'item' in Items block.")
                advance()
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after Items block.")
    }

    private fun itemDecl(): Decl.ItemDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect item name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after item name.")

        val fields = mutableListOf<ItemField>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            fields.add(itemField())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after item body.")
        return Decl.ItemDecl(name, fields)
    }

    private fun itemField(): ItemField {
        return when {
            match(TokenType.EFFECT) -> {
                consume(TokenType.COLON, "Expect ':' after 'effect'.")
                consume(TokenType.PIPE_GREATER, "Expect '|>' to start pipeline.")
                val calls = mutableListOf<FunctionCall>()
                calls.add(functionCall())
                while (match(TokenType.PIPE_GREATER)) {
                    calls.add(functionCall())
                }
                ItemField.EffectField(PipelineExpr(calls))
            }
            check(TokenType.IDENTIFIER) -> {
                val name = advance()
                consume(TokenType.COLON, "Expect ':' after property name.")
                val value = expression()
                ItemField.PropertyField(name, value)
            }
            else -> {
                error(peek(), "Unexpected token in item body.")
                advance()
                ItemField.PropertyField(
                    Token(TokenType.IDENTIFIER, "error", null, peek().line),
                    Expr.Literal(null)
                )
            }
        }
    }

    // ---- Creeps Block ----
    private fun parseCreepsBlock(creeps: MutableList<Decl.CreepDecl>) {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'Creeps'.")

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.CREEP)) {
                creeps.add(creepDecl())
            } else {
                error(peek(), "Expect 'creep' in Creeps block.")
                advance()
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after Creeps block.")
    }

    private fun creepDecl(): Decl.CreepDecl {
        val name = consume(TokenType.IDENTIFIER, "Expect creep name.")
        consume(TokenType.LEFT_BRACE, "Expect '{' after creep name.")

        val stats = mutableListOf<StatEntry>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stats.add(statEntry())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after creep stats.")
        return Decl.CreepDecl(name, stats)
    }

    // ---- Expression Parsing (for :evaluate command) ----
    fun parseExpression(): Expr {
        val expr = expression()
        if (!isAtEnd()) {
            error(peek(), "Unexpected tokens after expression.")
        }
        return expr
    }

    // ---- Expressions ----
    private fun expression(): Expr = equality()

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
        var expr = primary()
        while (match(TokenType.STAR, TokenType.DIVIDE)) {
            val operator = previous()
            val right = primary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun primary(): Expr {
        if (match(TokenType.NUMBER)) return Expr.Literal(previous().lexeme.toDouble())
        if (match(TokenType.STRING)) return Expr.Literal(previous().literal)
        if (match(TokenType.PERCENTAGE)) {
            val value = previous().lexeme.removeSuffix("%").toDouble()
            return Expr.Percentage(value)
        }
        if (match(TokenType.TIME)) {
            val value = previous().lexeme.removeSuffix("s").toInt()
            return Expr.Time(value)
        }
        if (match(TokenType.IDENTIFIER)) {
            val name = previous()
            // Check if it's a function call
            if (match(TokenType.LEFT_PAREN)) {
                current-- // backtrack
                return Expr.FunctionCallExpr(functionCall())
            }
            return Expr.Variable(name)
        }
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
}