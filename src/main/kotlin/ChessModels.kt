enum class Piece(val pieceSymbol: String, val pieceLabel: String)
{
    PAWN("♟", "Pawn"),
    ROOK("♜", "Rook"),
    KNIGHT("♞", "Knight"),
    BISHOP("♝", "Bishop"),
    QUEEN("♛", "Queen"),
    KING("♚", "King");

    fun getSymbol(): String = pieceSymbol
    fun getLabel(): String = pieceLabel
}

enum class Player
{
    WHITE,
    BLACK
}

data class ChessPiece(
    var type: Piece,
    var player: Player
)

enum class MoveType
{
    NORMAL,
    CASTLE_KINGS_SIDE,
    CASTLE_QUEENS_SIDE,
    EN_PASSANT
}

data class MoveOptions(
    val moves: List<Pair<Pair<Int, Int>, MoveType>>,
    val captures: List<Pair<Int, Int>>
)

data class CheckState(
    val isCheck: Boolean,
    val kingPosition: Pair<Int, Int>?
)

enum class GameState
{
    PLAYING,
    RESIGNED,
    CHECKMATE,
    STALEMATE,
    DRAW,
    TIMEOUT
}

data class CastlingRights(
    var whiteKingSide: Boolean,
    var whiteQueenSide: Boolean,
    var blackKingSide: Boolean,
    var blackQueenSide: Boolean
)

enum class CastlingSquare(val row: Int, val col: Int)
{
    BLACK_QUEEN_ROOK(0, 0),
    BLACK_KING_ROOK(0, 7),
    BLACK_KING(0, 4),

    WHITE_QUEEN_ROOK(7, 0),
    WHITE_KING_ROOK(7, 7),
    WHITE_KING(7, 4)
}

data class Move(
    val index: Int,
    val from: Pair<Int, Int>,
    val to: Pair<Int, Int>,
    val movingPiece: ChessPiece?,
    val capturedPiece: Boolean,
    val promotion: Piece?,
    val type: MoveType?,
    val check: Boolean,
    val end: GameState,
    val player: Player
)