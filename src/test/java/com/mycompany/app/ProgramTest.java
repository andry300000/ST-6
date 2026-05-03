package com.mycompany.app;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProgramTest {
  @BeforeClass
  public static void setHeadlessMode() {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  public void gameConstructorInitializesBoardAndPlayers() {
    Game game = new Game();

    assertEquals(State.PLAYING, game.state);
    assertEquals('X', game.player1.symbol);
    assertEquals('O', game.player2.symbol);
    assertEquals(9, game.board.length);
    assertArrayEquals(new char[] {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, game.board);
  }

  @Test
  public void checkStateHandlesWinsDrawAndPlaying() {
    Game game = new Game();

    game.symbol = 'X';
    assertEquals(State.XWIN, game.checkState(new char[] {'X', 'X', 'X', ' ', ' ', ' ', ' ', ' ', ' '}));

    game.symbol = 'O';
    assertEquals(State.OWIN, game.checkState(new char[] {'O', 'X', 'X', 'X', 'O', ' ', 'X', ' ', 'O'}));

    game.symbol = 'X';
    assertEquals(State.PLAYING, game.checkState(new char[] {'X', 'O', 'X', 'O', ' ', ' ', ' ', ' ', ' '}));

    game.symbol = 'X';
    assertEquals(State.DRAW, game.checkState(new char[] {'X', 'O', 'X', 'X', 'O', 'O', 'O', 'X', 'X'}));
  }

  @Test
  public void generateMovesReturnsAllEmptyPositions() {
    Game game = new Game();
    ArrayList<Integer> moves = new ArrayList<>();

    game.generateMoves(new char[] {'X', ' ', 'O', ' ', 'X', ' ', 'O', 'X', ' '}, moves);

    assertEquals(4, moves.size());
    assertEquals(Integer.valueOf(1), moves.get(0));
    assertEquals(Integer.valueOf(3), moves.get(1));
    assertEquals(Integer.valueOf(5), moves.get(2));
    assertEquals(Integer.valueOf(8), moves.get(3));
  }

  @Test
  public void evaluatePositionReturnsExpectedScores() {
    Game game = new Game();
    Player xPlayer = game.player1;
    Player oPlayer = game.player2;

    game.symbol = 'X';
    assertEquals(Game.INF, game.evaluatePosition(
        new char[] {'X', 'X', 'X', 'O', 'O', ' ', ' ', ' ', ' '}, xPlayer));
    assertEquals(-Game.INF, game.evaluatePosition(
        new char[] {'X', 'X', 'X', 'O', 'O', ' ', ' ', ' ', ' '}, oPlayer));

    game.symbol = 'O';
    assertEquals(0, game.evaluatePosition(
        new char[] {'X', 'O', 'X', 'X', 'O', 'O', 'O', 'X', 'X'}, xPlayer));

    game.symbol = 'X';
    assertEquals(-1, game.evaluatePosition(
        new char[] {'X', 'O', ' ', ' ', 'O', ' ', ' ', 'X', ' '}, xPlayer));
  }

  @Test
  public void miniMaxFindsWinningMoveAndResetsCounter() {
    Game game = new Game();

    int move = game.miniMax(new char[] {'O', 'O', ' ', 'X', 'X', ' ', ' ', ' ', ' '}, game.player2);

    assertEquals(3, move);
    assertEquals(0, game.q);
  }

  @Test
  public void minAndMaxMoveReturnTerminalScores() {
    Game game = new Game();

    game.symbol = 'X';
    assertEquals(Game.INF, game.maxMove(
        new char[] {'X', 'X', 'X', 'O', ' ', 'O', ' ', ' ', ' '}, game.player1));

    game.symbol = 'O';
    assertEquals(-Game.INF, game.minMove(
        new char[] {'O', 'O', 'O', 'X', ' ', 'X', ' ', ' ', ' '}, game.player1));
  }

  @Test
  public void ticTacToeCellStoresCoordinatesAndMarker() {
    TicTacToeCell cell = new TicTacToeCell(7, 1, 2);

    assertEquals(7, cell.getNum());
    assertEquals(2, cell.getRow());
    assertEquals(1, cell.getCol());
    assertEquals(' ', cell.getMarker());

    cell.setMarker("X");

    assertEquals('X', cell.getMarker());
    assertEquals("X", cell.getText());
    assertFalse(cell.isEnabled());
  }

  @Test
  public void utilityPrintMethodsWriteValuesToStdout() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    try {
      System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));

      Utility.print(new char[] {'X', 'O', 'X', ' ', ' ', ' ', ' ', ' ', ' '});
      Utility.print(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9});
      ArrayList<Integer> moves = new ArrayList<>();
      moves.add(1);
      moves.add(5);
      moves.add(8);
      Utility.print(moves);
    } catch (Exception exception) {
      throw new AssertionError(exception);
    } finally {
      System.setOut(originalOut);
    }

    String text = new String(output.toByteArray(), StandardCharsets.UTF_8).replace("\r", "");
    assertTrue(text.contains("X-O-X- - - - - - -"));
    assertTrue(text.contains("1-2-3-4-5-6-7-8-9-"));
    assertTrue(text.contains("1-5-8-"));
  }

  @Test
  public void panelConstructorCreatesBoardAndSetsCurrentPlayer() {
    TestableTicTacToePanel panel = new TestableTicTacToePanel();

    assertEquals(9, panel.getComponentCount());
    assertNotNull(panel.getCells());
    assertEquals(9, panel.getCells().length);
    assertEquals(panel.getGame().player1, panel.getGame().cplayer);
  }

  @Test
  public void actionPerformedHandlesSecondPlayerTurnWithoutEndingGame() {
    TestableTicTacToePanel panel = new TestableTicTacToePanel();
    panel.getGame().cplayer = panel.getGame().player2;

    TicTacToeCell targetCell = panel.getCells()[0];
    panel.actionPerformed(new ActionEvent(targetCell, ActionEvent.ACTION_PERFORMED, "click"));

    assertEquals('O', targetCell.getMarker());
    assertEquals('O', panel.getGame().board[0]);
    assertEquals(panel.getGame().player1, panel.getGame().cplayer);
    assertEquals(State.PLAYING, panel.getGame().state);
    assertEquals(0, panel.finishCalls);
  }

  @Test
  public void actionPerformedHandlesDrawWithoutRealExit() {
    TestableTicTacToePanel panel = new TestableTicTacToePanel();
    TicTacToeCell[] cells = panel.getCells();

    cells[0].setMarker("X");
    cells[1].setMarker("O");
    cells[2].setMarker("X");
    cells[3].setMarker("X");
    cells[4].setMarker("O");
    cells[5].setMarker("O");
    cells[6].setMarker("O");
    cells[7].setMarker("X");

    panel.actionPerformed(new ActionEvent(cells[8], ActionEvent.ACTION_PERFORMED, "click"));

    assertEquals('X', cells[8].getMarker());
    assertEquals(State.DRAW, panel.getGame().state);
    assertEquals(State.DRAW, panel.lastFinishedState);
    assertEquals(1, panel.finishCalls);
  }

  private static class TestableTicTacToePanel extends TicTacToePanel {
    private State lastFinishedState;
    private int finishCalls;

    TestableTicTacToePanel() {
      super(new GridLayout(3, 3));
    }

    @Override
    protected void finishGame(State state) {
      lastFinishedState = state;
      finishCalls++;
    }
  }
}
