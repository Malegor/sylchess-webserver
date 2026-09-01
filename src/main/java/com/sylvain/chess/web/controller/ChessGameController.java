package com.sylvain.chess.web.controller;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.io.fen.FenSaver;
import com.sylvain.chess.play.Gameplay;
import com.sylvain.chess.web.players.WebInteractivePlayer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@Controller
public class ChessGameController {

  private final SimpMessagingTemplate messagingTemplate;
  private Gameplay gameplay;
  private WebInteractivePlayer whitePlayer;
  private WebInteractivePlayer blackPlayer;

  public ChessGameController(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/start")
  public void startGame() {
    final ChessBoard board = ChessBoard.defaultBoard();
    this.gameplay = new Gameplay(board);
    final CountDownLatch latch = new CountDownLatch(1);
    // Pass-and-play setup using our new web player
    this.whitePlayer = new WebInteractivePlayer(PlayerColor.WHITE, "Human White", board, latch);
    this.blackPlayer = new WebInteractivePlayer(PlayerColor.BLACK, "Human Black", board, latch);
    // Run your existing Gameplay while loop on an independent thread
    new Thread(() -> {
      this.gameplay.playGame(List.of(this.whitePlayer, this.blackPlayer));
    }).start();
    this.broadcastState();
  }

  @MessageMapping("/move")
  public void receiveMove(final String pgnOrSanMove) {
    // Find whose turn it currently is and give them the text move
    final WebInteractivePlayer playerToMove = this.gameplay.getInfo().getLastPlayer().equals(this.blackPlayer) ? this.whitePlayer : this.blackPlayer;
    playerToMove.submitWebMove(pgnOrSanMove);
    // Wait a tiny moment for the engine thread to validate and update
    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
    this.broadcastState();
  }

  private void broadcastState() {
    final String currentFen = FenSaver.getPositionString(this.gameplay.getInfo(), this.gameplay.getBoard());
    // Collect current turn active errors to display in React
    final String activeError = this.whitePlayer.getLastErrorMessage().equals(" ")
            ? this.blackPlayer.getLastErrorMessage()
            : this.whitePlayer.getLastErrorMessage();
    // Push layout information to React
    this.messagingTemplate.convertAndSend("/topic/game-update", Optional.of(Map.of(
            "fen", currentFen,
            "error", activeError
    )));
  }
}
