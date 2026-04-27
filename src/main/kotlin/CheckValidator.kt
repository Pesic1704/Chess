import kotlin.collections.map

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

        val kingPos = findKing(board,enemy)

        val validator= MoveValidator(board)

        val moves = validator.getPseudoLegalMoves(row, col).moves.map { it.first }

        return kingPos in moves
    }
    fun isOpponentCheckmatedByPlayer(player: Player): Boolean
    {
        val opponent = if (player == Player.WHITE) Player.BLACK else Player.WHITE

        if (!isPlayerGivingCheck(player))
        {
            return false
        }

        for (row in 0..7)
        {
            for (col in 0..7)
            {
                val piece = board.grid[row][col] ?: continue
                if (piece.player != opponent) continue

                val validator= MoveValidator(board)

                val moves = validator.getLegalMoves( row, col).moves.map { it.first }

                if(moves.isNotEmpty())
                {
                    return false
                }
            }
        }

        return true
    }
    fun isStalemateCausedByPlayer(player: Player): Boolean
    {
        if (isPlayerGivingCheck(player))
        {
            return false
        }

        for (row in 0..7)
        {
            for (col in 0..7)
            {
                val piece = board.grid[row][col] ?: continue
                if (piece.player == player) continue

                val validator= MoveValidator(board)
                val moves = validator.getLegalMoves( row, col).moves.map { it.first }

                if (moves.isNotEmpty())
                {
                    return false
                }
            }
        }

        return true
    }
}