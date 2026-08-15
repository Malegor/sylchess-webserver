package com.sylvain.chess.web.players;

import com.sylvain.chess.PlayerColor;
import com.sylvain.chess.board.ChessBoard;
import com.sylvain.chess.moves.Move;
import com.sylvain.chess.play.players.interactive.GuiInteractivePlayer;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WebInteractivePlayer extends GuiInteractivePlayer {
  private String nextMoveText;
  private String lastErrorMessage = " ";

  public WebInteractivePlayer(final PlayerColor color, final String name, final ChessBoard board, final CountDownLatch waitingForNextMove)
  {
    super(color, name, board, waitingForNextMove);
  }

  // React calls this method through the WebSocket when a human plays
  public synchronized void submitWebMove(final String moveStr) {
    this.nextMoveText = moveStr;
    this.getWaitingForNextMove().countDown(); // Wakes up getNextMove() below!
  }

  @Override
  protected String getNextMove() {
    this.resetWaitForNextMove();
    try {
      this.getWaitingForNextMove().await(); // Safely freezes the loop until the web user interacts
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    this.lastErrorMessage = " "; // Reset errors for the new turn
    return this.nextMoveText;
  }

  @Override
  protected void handleInvalidMove(List<Move> validMoves, String moveStr) {
    super.handleInvalidMove(validMoves, moveStr);
    // Save the error so we can send it to the browser text banner
    this.lastErrorMessage = "Invalid move: \"" + moveStr + "\"";
  }

  public String getLastErrorMessage() {
    return this.lastErrorMessage;
  }
}
