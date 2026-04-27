class DrawValidator(private val board: Board,
                    private val boardState: Long,
                    private val boardStateHistory: Map<Long, Int>,
                    private val fiftyMoveCounter: Int)
{
    fun isDraw(): Boolean
    {
        if(isInsufficientMaterial())
        {
            return true
        }
        if(threeFoldRepetition())
        {
            return true
        }
        if(fiftyMoveRule())
        {
            return true
        }

        return false
    }
    fun isInsufficientMaterial(): Boolean
    {
        val pieces = mutableListOf< Pair<ChessPiece, Pair<Int,Int>> >()

        for (row in 0..7)
        {
            for (col in 0..7)
            {
                val piece = board.grid[row][col]
                if (piece != null && piece.type != Piece.KING)
                {
                    pieces.add(piece to (row to col))
                }
            }
        }

        if (pieces.isEmpty()) return true

        if (pieces.size == 1 && (pieces[0].first.type == Piece.KNIGHT || pieces[0].first.type == Piece.BISHOP))
        {
            return true
        }

        if (pieces.size == 2 &&
            pieces[0].first.type == Piece.BISHOP && pieces[1].first.type == Piece.BISHOP &&
            pieces[0].first.player != pieces[1].first.player &&
            isWhiteSquare(pieces[0].second.first,pieces[0].second.second) ==
            isWhiteSquare(pieces[1].second.first,pieces[1].second.second)
        )
        {
            return true
        }

        return false
    }
    fun threeFoldRepetition(): Boolean
    {
        return boardStateHistory[boardState] == 3
    }
    fun fiftyMoveRule(): Boolean
    {
        return fiftyMoveCounter==50
    }
}