// Program.kt
//
// Top-level AST node representing the entire DSL program.
// Cleaned & corrected to match Parser, Evaluator, and AstPrinter.
//

data class Program(
    val name: Token,

    // imports: import XYZ;
    val imports: List<Decl.ImportDecl>,

    // top-level variables (set x = ...)
    val variables: List<Decl.VarDecl>,

    // heroes block
    val heroes: List<Decl.HeroDecl>,

    // arena items: turrets, cores (standalone, not part of teams)
    val arenaItems: List<Decl>,

    // teams inside arena
    val teams: List<Decl.TeamDecl>,

    // status effects block
    val statusEffects: List<Decl.StatusEffectDecl>,

    // items block
    val items: List<Decl.ItemDecl>,

    // creeps block
    val creeps: List<Decl.CreepDecl>,

    // functions block
    val functions: List<Decl.FunctionDecl>
)
