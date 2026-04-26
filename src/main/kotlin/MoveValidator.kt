class MoveValidator(private val board: Board)
{
    fun getLegalMoves(row: Int, col: Int): MoveOptions
    {
        val piece = board.grid[row][col] ?: return MoveOptions(emptyList(), emptyList())

        val pseudoResults = getPseudoLegalMoves(row, col)

        val legalMoves = mutableListOf<Pair<Pair<Int, Int>, Int>> ()
        val legalCaptures = mutableListOf<Pair<Int, Int>> ()

        for ((pos, type) in pseudoResults.moves)
        {
            val (toRow, toCol) = pos

            val copy = board.copy()

            copy.grid[toRow][toCol] = piece
            copy.grid[row][col] = null

           // TODO en passant and castling

            val opponent = if (piece.player == Player.WHITE) Player.BLACK else Player.WHITE

            val validator = CheckValidator(copy)

            if (!validator.isPlayerGivingCheck(opponent))
            {
                legalMoves.add(toRow to toCol to type)

                if((toRow to toCol) in pseudoResults.captures)
                {
                    legalCaptures.add(toRow to toCol)
                }
            }
        }

        return MoveOptions(legalMoves.toList(), legalCaptures.toList())
    }
    fun getPseudoLegalMoves(row: Int, col: Int ): MoveOptions
    {
        val piece = board.grid[row][col] ?: return MoveOptions(emptyList(), emptyList())

        return when (piece.type)
        {
            Piece.ROOK -> getRookMoves( row, col)
            Piece.BISHOP -> getBishopMoves( row, col)
            Piece.QUEEN -> getQueenMoves( row, col)
            Piece.KNIGHT -> getKnightMoves( row, col)
            Piece.PAWN -> getPawnMoves( row, col)
            Piece.KING -> getKingMoves( row, col)

            // TODO en passant and castling
        }
    }

    fun getRookMoves( row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Int>> ()
        val captures = mutableListOf<Pair<Int, Int>> ()

        val piece = board.grid[row][col] ?: return MoveOptions(moves.toList(),captures.toList())

        val directions = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

        for ((dirRow, dirCol) in directions)
        {

            var newRow = row + dirRow
            var newCol = col + dirCol

            while (newRow in 0..7 && newCol in 0..7)
            {

                val target = board.grid[newRow][newCol]

                if (target == null)
                {
                    moves.add(newRow to newCol to 0)
                }
                else
                {
                    if (target.player != piece.player)
                    {
                        moves.add(newRow to newCol to 0)
                        captures.add(newRow to newCol)
                    }

                    break
                }

                newRow += dirRow
                newCol += dirCol
            }
        }

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getBishopMoves(row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Int>> ()
        val captures = mutableListOf<Pair<Int, Int>> ()

        val piece = board.grid[row][col] ?: return MoveOptions(moves.toList(),captures.toList())

        val directions = listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)

        for ((dirRow, dirCol) in directions)
        {
            var newRow = row + dirRow
            var newCol = col + dirCol

            while (newRow in 0..7 && newCol in 0..7) {

                val target = board.grid[newRow][newCol]

                if (target == null)
                {
                    moves.add(newRow to newCol to 0)
                }
                else
                {
                    if (target.player != piece.player)
                    {
                        moves.add(newRow to newCol to 0)
                        captures.add(newRow to newCol)
                    }
                    break
                }

                newRow += dirRow
                newCol += dirCol
            }
        }

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getQueenMoves( row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Int>>()
        val captures = mutableListOf<Pair<Int, Int>> ()

        moves.addAll(getRookMoves(row, col).moves)
        captures.addAll(getRookMoves(row, col).captures)

        moves.addAll(getBishopMoves(row, col ).moves)
        captures.addAll(getBishopMoves(row, col).captures)

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getKnightMoves(row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Int>>()
        val captures = mutableListOf<Pair<Int, Int>> ()

        val piece = board.grid[row][col] ?: return MoveOptions(moves.toList(),captures.toList())

        val directions = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)

        for ((dirRow, dirCol) in directions)
        {
            val newRow = row + dirRow
            val newCol = col + dirCol

            if (newRow in 0..7 && newCol in 0..7) {

                val target = board.grid[newRow][newCol]

                if (target == null)
                {
                    moves.add(newRow to newCol to 0)
                }
                else
                {
                    if (target.player != piece.player)
                    {
                        moves.add(newRow to newCol to 0)
                        captures.add(newRow to newCol)
                    }
                }
            }
        }

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getPawnMoves( row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Int>> ()
        val captures = mutableListOf<Pair<Int, Int>> ()

        val piece = board.grid[row][col] ?: return MoveOptions(moves.toList(),captures.toList())

        val direction = if (piece.player == Player.WHITE) -1 else 1
        val startRow = if (piece.player == Player.WHITE) 6 else 1

        val oneStep = row + direction
        val twoStep = row + 2 * direction

        if (oneStep in 0..7 && board.grid[oneStep][col] == null)
        {
            moves.add((oneStep to col) to 0)
            if (row == startRow && board.grid[twoStep][col] == null)
            {
                moves.add((twoStep to col) to 0)
            }
        }

        val directions = listOf(-1,1)

        for (dirCol in directions)
        {
            val newCol = col + dirCol

            if (oneStep in 0..7 && newCol in 0..7)
            {
                val target = board.grid[oneStep][newCol]

                if (target != null && target.player != piece.player)
                {
                    moves.add((oneStep to newCol) to 0)
                    captures.add(oneStep to newCol)
                }
            }
        }

        // TODO en passant

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getKingMoves(row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Int>> ()
        val captures = mutableListOf<Pair<Int, Int>> ()

        val piece = board.grid[row][col] ?: return MoveOptions(moves.toList(),captures.toList())

        val directions = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1,  1 to 0,  1 to 1)

        for ((dirRow, dirCol) in directions) {

            val newRow = row + dirRow
            val newCol = col + dirCol

            if (newRow in 0..7 && newCol in 0..7) {

                val target = board.grid[newRow][newCol]

                if (target == null)
                {
                    moves.add(newRow to newCol to 0)
                }
                else if (target.player != piece.player)
                {
                    moves.add(newRow to newCol to 0)
                    captures.add(newRow to newCol)
                }
            }
        }

        // TODO castling

        return MoveOptions(moves.toList(),captures.toList())
    }
}