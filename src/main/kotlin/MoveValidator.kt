class MoveValidator(private val board: Board)
{
    fun getLegalMoves(row: Int, col: Int): MoveOptions
    {
        val piece = board.grid[row][col] ?: return MoveOptions(emptyList(), emptyList())

        val pseudoResults = getPseudoLegalMoves(row, col)

        val legalMoves = mutableListOf<Pair<Pair<Int, Int>, MoveType>> ()
        val legalCaptures = mutableListOf<Pair<Int, Int>> ()

        for ((pos, type) in pseudoResults.moves)
        {
            val (toRow, toCol) = pos
            val opponent = if (piece.player == Player.WHITE) Player.BLACK else Player.WHITE

            var isLegal = true

            if(type == MoveType.NORMAL)
            {
                val tempBoard = board.clone()

                tempBoard.grid[toRow][toCol] = piece
                tempBoard.grid[row][col] = null

                val validator = CheckValidator(tempBoard)
                if (validator.isPlayerGivingCheck(opponent))
                {
                    isLegal = false
                }
            }
            else if (type == MoveType.CASTLE_QUEENS_SIDE)
            {
                for(tempCol in 2 .. 4)
                {
                    val tempBoard = board.clone()

                    tempBoard.grid[toRow][tempCol] = piece
                    tempBoard.grid[row][col] = if(col == tempCol) piece else null

                    val validator = CheckValidator(tempBoard)
                    if (validator.isPlayerGivingCheck(opponent))
                    {
                        isLegal = false
                        break
                    }
                }
            }
            else if (type == MoveType.CASTLE_KINGS_SIDE)
            {
                for(tempCol in 4 .. 6)
                {
                    val tempBoard = board.clone()

                    tempBoard.grid[toRow][tempCol] = piece
                    tempBoard.grid[row][col] = if(col == tempCol) piece else null

                    val validator = CheckValidator(tempBoard)
                    if (validator.isPlayerGivingCheck(opponent))
                    {
                        isLegal = false
                        break
                    }
                }
            }
            else if (type == MoveType.EN_PASSANT)
            {
                val tempBoard = board.clone()

                tempBoard.grid[toRow][toCol] = piece
                tempBoard.grid[row][col] = null
                tempBoard.grid[row][toCol] = null

                val validator = CheckValidator(tempBoard)
                if (validator.isPlayerGivingCheck(opponent))
                {
                    isLegal = false
                }
            }

            if (isLegal)
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
        }
    }

    fun getRookMoves( row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, MoveType>> ()
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
                    moves.add(newRow to newCol to MoveType.NORMAL)
                }
                else
                {
                    if (target.player != piece.player)
                    {
                        moves.add(newRow to newCol to MoveType.NORMAL)
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
        val moves = mutableListOf<Pair<Pair<Int, Int>, MoveType>> ()
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
                    moves.add(newRow to newCol to MoveType.NORMAL)
                }
                else
                {
                    if (target.player != piece.player)
                    {
                        moves.add(newRow to newCol to MoveType.NORMAL)
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
        val moves = mutableListOf<Pair<Pair<Int, Int>, MoveType>>()
        val captures = mutableListOf<Pair<Int, Int>> ()

        moves.addAll(getRookMoves(row, col).moves)
        captures.addAll(getRookMoves(row, col).captures)

        moves.addAll(getBishopMoves(row, col ).moves)
        captures.addAll(getBishopMoves(row, col).captures)

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getKnightMoves(row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, MoveType>>()
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
                    moves.add(newRow to newCol to MoveType.NORMAL)
                }
                else
                {
                    if (target.player != piece.player)
                    {
                        moves.add(newRow to newCol to MoveType.NORMAL)
                        captures.add(newRow to newCol)
                    }
                }
            }
        }

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getPawnMoves( row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, MoveType>> ()
        val captures = mutableListOf<Pair<Int, Int>> ()

        val piece = board.grid[row][col] ?: return MoveOptions(moves.toList(),captures.toList())

        val moveDirection = if (piece.player == Player.WHITE) -1 else 1
        val startRow = if (piece.player == Player.WHITE) 6 else 1

        val oneStep = row + moveDirection
        val twoStep = row + 2 * moveDirection

        if (oneStep in 0..7 && board.grid[oneStep][col] == null)
        {
            moves.add((oneStep to col) to MoveType.NORMAL)
            if (row == startRow && board.grid[twoStep][col] == null)
            {
                moves.add((twoStep to col) to MoveType.NORMAL)
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
                    moves.add((oneStep to newCol) to MoveType.NORMAL)
                    captures.add(oneStep to newCol)
                }
            }
        }

        if (board.enPassantTarget != null)
        {
            if (((row+moveDirection) to (col-1)) == board.enPassantTarget)
            {
                moves.add(Pair(Pair(row + moveDirection, col - 1), MoveType.EN_PASSANT))
                captures.add(Pair(row + moveDirection, col - 1))
            }
            if (((row+moveDirection) to (col+1)) == board.enPassantTarget)
            {
                moves.add(Pair(Pair(row + moveDirection, col + 1), MoveType.EN_PASSANT))
                captures.add(Pair(row + moveDirection, col + 1))
            }
        }

        return MoveOptions(moves.toList(),captures.toList())
    }
    fun getKingMoves(row: Int, col: Int): MoveOptions
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, MoveType>> ()
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
                    moves.add(newRow to newCol to MoveType.NORMAL)
                }
                else if (target.player != piece.player)
                {
                    moves.add(newRow to newCol to MoveType.NORMAL)
                    captures.add(newRow to newCol)
                }
            }
        }

        val startRow = if (piece.player == Player.WHITE) 7 else 0

        if(piece.player == Player.WHITE)
        {
            if(board.castlingRights.whiteQueenSide)
            {
                if(board.grid[startRow][1] == null && board.grid[startRow][2] == null && board.grid[startRow][3] == null)
                {
                    moves.add(Pair(Pair(startRow, col - 2), MoveType.CASTLE_QUEENS_SIDE))
                }
            }
            if(board.castlingRights.whiteKingSide)
            {
                if(board.grid[startRow][5] == null && board.grid[startRow][6] == null)
                {
                    moves.add(Pair(Pair(startRow, col + 2), MoveType.CASTLE_KINGS_SIDE))
                }
            }
        }
        else
        {
            if(board.castlingRights.blackQueenSide)
            {
                if(board.grid[startRow][1] == null && board.grid[startRow][2] == null && board.grid[startRow][3] == null)
                {
                    moves.add(Pair(Pair(startRow, col - 2), MoveType.CASTLE_QUEENS_SIDE))
                }
            }
            if(board.castlingRights.blackKingSide)
            {
                if(board.grid[startRow][5] == null && board.grid[startRow][6] == null)
                {
                    moves.add(Pair(Pair(startRow, col + 2), MoveType.CASTLE_KINGS_SIDE))
                }
            }
        }

        return MoveOptions(moves.toList(),captures.toList())
    }
}