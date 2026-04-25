fun findKing(board: Board,player: Player) : Pair<Int, Int>?
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
