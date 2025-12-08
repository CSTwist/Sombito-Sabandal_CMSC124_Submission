// Decl.kt
//
// All declaration nodes for the DSL.
// Cleaned, standardized, and corrected to match the parser and evaluator.
//

sealed class Decl {

    // ------------------------- IMPORT -------------------------
    data class ImportDecl(val name: Token) : Decl()

    // ------------------------- VARIABLES -----------------------
    data class VarDecl(val name: Token, val value: Expr) : Decl()

    // ------------------------- HEROES --------------------------
    data class HeroDecl(val name: Token, val statements: List<HeroStatement>) : Decl()

    // ------------------------- ARENA ---------------------------
    data class TeamDecl(
        val name: Token,
        val coreRef: Token?,
        val turrets: List<TurretDecl>
    ) : Decl()

    data class TurretDecl(val name: Token, val stats: List<StatEntry>) : Decl()

    data class CoreDecl(val name: Token, val stats: List<StatEntry>) : Decl()

    // ------------------------- STATUS EFFECTS -------------------
    data class StatusEffectDecl(
        val name: Token,
        val fields: List<StatusEffectField>
    ) : Decl()

    // ------------------------- ITEMS ----------------------------
    data class ItemDecl(
        val name: Token,
        val fields: List<ItemField>
    ) : Decl()

    // ------------------------- CREEPS ---------------------------
    data class CreepDecl(
        val name: Token,
        val stats: List<StatEntry>
    ) : Decl()

    // ------------------------- FUNCTIONS ------------------------
    data class FunctionDecl(
        val name: Token,
        val params: List<Param>,
        val returnType: Token?,
        val body: BlockStmt
    ) : Decl()
}

// ============================================================
// HERO STATEMENTS
// ============================================================

sealed class HeroStatement {
    data class SetStmt(val name: Token, val value: Expr) : HeroStatement()

    data class HeroStatBlock(val stats: List<StatEntry>) : HeroStatement()

    data class AbilitiesBlock(val abilities: List<AbilityDecl>) : HeroStatement()
}

// ============================================================
// ABILITIES
// ============================================================

data class AbilityDecl(
    val name: Token,
    val fields: List<AbilityField>
)

sealed class AbilityField {
    data class TypeField(val value: Token) : AbilityField()
    data class CooldownField(val value: Expr) : AbilityField()
    data class ManaCostField(val value: Expr) : AbilityField()
    data class RangeField(val value: Expr) : AbilityField()
    data class DamageTypeField(val value: Token) : AbilityField()

    data class BehaviorField(
        val body: BlockStmt?,
        val expression: Expr?
    ) : AbilityField()
}

// ============================================================
// STATUS EFFECT FIELDS
// ============================================================

sealed class StatusEffectField {
    data class TypeField(val value: Token) : StatusEffectField()
    data class DurationField(val value: Expr) : StatusEffectField()
    data class OnApplyField(val block: BlockStmt) : StatusEffectField()
    data class OnTickField(val block: BlockStmt) : StatusEffectField()
    data class OnExpireField(val block: BlockStmt) : StatusEffectField()
}

// ============================================================
// ITEMS
// ============================================================

sealed class ItemField {
    data class PropertyField(val name: Token, val value: Expr) : ItemField()
    data class PassiveField(val behavior: Expr) : ItemField()
}

// ============================================================
// GENERIC SHARED STRUCTURES
// ============================================================

data class StatEntry(val name: Token, val value: Expr)

data class Param(val type: Token, val name: Token)

// ============================================================
// FUNCTION CALL ARGUMENTS
// ============================================================

sealed class Argument {
    data class NamedArg(val name: Token, val value: Expr) : Argument()
    data class PositionalArg(val value: Expr) : Argument()
}

data class FunctionCall(
    val name: Token,
    val arguments: List<Argument>
)

// ============================================================
// TARGET EXPRESSIONS (apply ... to <target>)
// ============================================================

sealed class TargetExpr {
    object Self : TargetExpr()
    object Target : TargetExpr()
    object Caster : TargetExpr()
    data class Named(val name: Token) : TargetExpr()
}
