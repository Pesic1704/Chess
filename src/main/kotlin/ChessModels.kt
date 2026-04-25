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

data class MoveResult(
    val moves: List<Pair<Pair<Int, Int>, Int>>,
    val captures: List<Pair<Int, Int>>
)