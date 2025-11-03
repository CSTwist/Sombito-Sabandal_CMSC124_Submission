// Decl.kt
sealed interface Decl {

    /** ganap IDENTIFIER ( [ ParamList ] ) Block */
    data class FunDecl(
        val returnType: Token?,        // may be null if no type is declared
        val name: Token,
        val params: List<Token>,
        val body: Stmt.Block,
        val otherBlock: Stmt.Block?    // null if not declared
    ) : Decl

    /** lagay IDENTIFIER [ = Expression ] ; */
    data class VarDecl(
        val name: Token,
        val initializer: Expr?
    ) : Decl

    /** peg IDENTIFIER [ = Expression ] ; */
    data class ConstDecl(
        val name: Token,
        val value: Expr?
    ) : Decl

    /** aray IDENTIFIER { { ArayMember } } ; */
    data class ArayDecl(
        val name: Token,
        val members: List<ArayMember>
    ) : Decl

    /** enum IDENTIFIER { EnumList } ; */
    data class EnumDecl(
        val name: Token,
        val entries: List<Token>
    ) : Decl

    // Helper class for struct-like array members
    data class ArayMember(
        val type: Token,       // e.g. DataType keyword or "aray"
        val name: Token,
        val size: Int?         // e.g. [10] → 10
    )
}