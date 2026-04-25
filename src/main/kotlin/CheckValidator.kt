class CheckValidator(private val board: Board)
{
    fun isPlayerGivingCheck(player: Player): Boolean
    {
        for (row in 0..7)
        {
            for (col in 0..7)
            {
                val piece = board.grid[row][col]

                if (piece != null && piece.player == player)
                {
                    if (isPieceGivingCheck(row, col))
                    {
                        return true
                    }
                }
            }
        }

        return false
    }

    fun isPieceGivingCheck(row: Int, col: Int): Boolean
    {
        val piece = board.grid[row][col] ?: return false

        val enemy = if (piece.player == Player.WHITE) Player.BLACK else Player.WHITE

        var kingPos = findKing(board,enemy)

        val validator= MoveValidator(board)

        val moves = validator.getPseudoLegalMoves(row, col).moves.map { it.first }

        return kingPos in moves
    }
}