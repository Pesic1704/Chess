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

enum class GameState
{
    PLAYING,
    RESIGNED,
    CHECKMATE,
    STALEMATE,
    DRAW,
    TIMEOUT
}

data class CheckState(
    val isCheck: Boolean,
    val kingPosition: Pair<Int, Int>?
)

data class CastlingRights(
    var whiteKingSide: Boolean,
    var whiteQueenSide: Boolean,
    var blackKingSide: Boolean,
    var blackQueenSide: Boolean
)