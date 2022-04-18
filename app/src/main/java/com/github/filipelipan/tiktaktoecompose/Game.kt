package com.github.filipelipan.tiktaktoecompose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class Game(
    val sendGameStateUpdate: (GameState) -> Unit,
    val sendBoardPositionsUpdate: (MutableList<BoardMark>) -> Unit,
) {

    private var gameState: GameState = GameState.Gaming
    private var currentMark: Mark = Mark.Ball
    private val boardPositions: MutableList<BoardMark> = mutableListOf()
    private val map: MutableMap<String, Int> = mutableMapOf()
    private val diagonal: MutableList<Mark> = mutableListOf()
    private val invertedDiagonal: MutableList<Mark> = mutableListOf()

    private val BOARD_SIZE = 2


    fun mark(boardPosition: BoardPosition) {
        if (boardPositions.none { it.boardPosition.x == boardPosition.x && it.boardPosition.y == boardPosition.y }
            && gameState == GameState.Gaming) {
            val xKey = boardPosition.x.toString().plus("x") + currentMark.toString()
            val yKey = boardPosition.y.toString().plus("y") + currentMark.toString()

            if (map.contains(xKey)) {
                map.put(xKey, map.get(xKey)?.plus(1) ?: 1)
            } else {
                map.put(xKey, 1)
            }
            if (map.contains(yKey)) {
                map.put(yKey, map.get(yKey)?.plus(1) ?: 1)
            } else {
                map.put(yKey, 1)
            }

            if (boardPosition.x == boardPosition.y) {
                diagonal.add(currentMark)
            }
            if ((boardPosition.x + boardPosition.y) == BOARD_SIZE) {
                invertedDiagonal.add(currentMark)
            }

            boardPositions.add(
                BoardMark(
                    boardPosition = boardPosition,
                    mark = currentMark
                )
            )
            sendBoardPositionsUpdate(boardPositions)

            checkWinCondition()
            currentMark = when (currentMark) {
                Mark.Ball -> Mark.Cross
                Mark.Cross -> Mark.Ball
            }
        }
    }

    fun restart() {
        map.clear()
        diagonal.clear()
        invertedDiagonal.clear()
        boardPositions.clear()
        currentMark = Mark.Ball
        updateGameState(GameState.Gaming)
        sendBoardPositionsUpdate(boardPositions)
    }

    private fun updateGameState(gameState: GameState) {
        this.gameState = gameState
        sendGameStateUpdate(gameState)
    }

    private fun checkWinCondition() {
        if (hasWon()) {
            updateGameState(GameState.Win(winnerMark = currentMark))
        } else if (boardPositions.size == 9) {
            updateGameState(GameState.Tie)
        }
    }

    private fun hasWon(): Boolean =
        hasWonByDiagonal() || hasWonByInvertedDiagonal() || hasWonByVerticalOrHorizontal()

    private fun hasWonByVerticalOrHorizontal() = !map.none { it.value > 2 }

    private fun hasWonByInvertedDiagonal() =
        invertedDiagonal.size == 3 && (invertedDiagonal.all { it == Mark.Ball } || invertedDiagonal.all { it == Mark.Cross })

    private fun hasWonByDiagonal() =
        diagonal.size == 3 && (diagonal.all { it == Mark.Ball } || diagonal.all { it == Mark.Cross })

}

sealed class GameState {
    object Gaming : GameState()
    data class Win(val winnerMark: Mark) : GameState()
    object Tie : GameState()
}

data class BoardPosition(
    val x: Int,
    val y: Int,
    val offset: Offset,
    val size: Size,
)

data class BoardMark(
    val boardPosition: BoardPosition,
    val mark: Mark
)

sealed class Mark {
    object Cross : Mark()
    object Ball : Mark()
}