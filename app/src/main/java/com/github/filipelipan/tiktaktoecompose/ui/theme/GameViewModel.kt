package com.github.filipelipan.tiktaktoecompose.ui.theme

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.github.filipelipan.tiktaktoecompose.BoardMark
import com.github.filipelipan.tiktaktoecompose.BoardPosition
import com.github.filipelipan.tiktaktoecompose.Game
import com.github.filipelipan.tiktaktoecompose.GameState

class GameViewModel : ViewModel() {

    var boardPositions = mutableStateListOf<BoardMark>()
        private set

    var gameState = mutableStateOf<GameState>(GameState.Gaming)
        private set

    private val game = Game(
        sendGameStateUpdate = { gameState.value = it },
        sendBoardPositionsUpdate = {
            boardPositions.clear()
            boardPositions.addAll(it)
        }
    )

    fun mark(boardPosition: BoardPosition) {
        game.mark(boardPosition = boardPosition)
    }

    fun restart() {
        game.restart()
    }
}