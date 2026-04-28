fun findKing(board: Board, player: Player) : Pair<Int, Int>?
{
    var piece:ChessPiece?

    for (row in 0..7)
    {
        for (col in 0..7)
        {
            piece = board.grid[row][col]

            if (piece!=null && piece.type == Piece.KING && piece.player == player)
            {
                return row to col
            }
        }
    }

    return null
}

fun isWhiteSquare(row: Int, col: Int): Boolean
{
    return (row + col) % 2 == 0
}

fun whoWon(player: Player?): String
{
    return when (player)
    {
        null -> "NO WINNER!"
        Player.WHITE -> "WHITE WON!"
        Player.BLACK -> "BLACK WON!"
    }
}

fun getPromotionPieces(): List<Piece>
{
    return arrayListOf(Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN)
}

fun colToString(col: Int): String
{
    return ('a' + col).toString()
}
