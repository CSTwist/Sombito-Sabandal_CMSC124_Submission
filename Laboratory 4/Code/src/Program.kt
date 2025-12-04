// Program.kt
data class Program(
    val name: Token,
    val imports: List<Decl.ImportDecl>,
    val variables: List<Decl.VarDecl>,
    val heroes: List<Decl.HeroDecl>,
    val arenaItems: List<Decl>,
    val teams: List<Decl.TeamDecl>,
    val statusEffects: List<Decl.StatusEffectDecl>,
    val items: List<Decl.ItemDecl>,
    val creeps: List<Decl.CreepDecl>,
    val functions: List<Decl.FunctionDecl>
)