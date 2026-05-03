package com.mycompany.app;

// Реализация игры "Крестики-нолики" (3x3)
// Минимаксный алгоритм

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

enum State { PLAYING, OWIN, XWIN, DRAW }

class Player {
  public char symbol;
  public int move;
  public boolean selected;
  public boolean win;
}

class Game {
  public State state;
  public Player player1;
  public Player player2;
  public Player cplayer; // текущий игрок
  public int nmove; // последний шаг сделанный действующим игроком
  public char symbol;
  public static final int INF = 100;
  public int q;
  public char[] board;

  public Game() {
    player1 = new Player();
    player2 = new Player();
    player1.symbol = 'X';
    player2.symbol = 'O';
    state = State.PLAYING;
    board = new char[9]; // текущая доска в игре
    for (int i = 0; i < 9; i++) {
      board[i] = ' ';
    }
  }

  // возвращаем состояние игры
  public State checkState(char[] board) {
    State currentState = State.PLAYING;
    if ((board[0] == symbol && board[1] == symbol && board[2] == symbol)
        || (board[3] == symbol && board[4] == symbol && board[5] == symbol)
        || (board[6] == symbol && board[7] == symbol && board[8] == symbol)
        || (board[0] == symbol && board[3] == symbol && board[6] == symbol)
        || (board[1] == symbol && board[4] == symbol && board[7] == symbol)
        || (board[2] == symbol && board[5] == symbol && board[8] == symbol)
        || (board[0] == symbol && board[4] == symbol && board[8] == symbol)
        || (board[2] == symbol && board[4] == symbol && board[6] == symbol)) {
      if (symbol == 'X') {
        currentState = State.XWIN;
      } else if (symbol == 'O') {
        currentState = State.OWIN;
      }
    } else {
      currentState = State.DRAW;
      for (int i = 0; i < 9; i++) {
        if (board[i] == ' ') {
          currentState = State.PLAYING;
          break;
        }
      }
    }
    return currentState;
  }

  // сгенерировать возможные ходы
  void generateMoves(char[] board, ArrayList<Integer> moveList) {
    for (int i = 0; i < 9; i++) {
      if (board[i] == ' ') {
        moveList.add(i);
      }
    }
  }

  // оценка позиции
  int evaluatePosition(char[] board, Player player) {
    State currentState = checkState(board);
    if (currentState == State.XWIN || currentState == State.OWIN || currentState == State.DRAW) {
      if ((currentState == State.XWIN && player.symbol == 'X')
          || (currentState == State.OWIN && player.symbol == 'O')) {
        return Game.INF;
      } else if ((currentState == State.XWIN && player.symbol == 'O')
          || (currentState == State.OWIN && player.symbol == 'X')) {
        return -Game.INF;
      } else if (currentState == State.DRAW) {
        return 0;
      }
    }
    return -1;
  }

  int miniMax(char[] board, Player player) { // выбор наилучшего хода
    int bestVal = -Game.INF;
    int index = 0;
    ArrayList<Integer> moveList = new ArrayList<>();
    int[] bestMoves = new int[9];

    generateMoves(board, moveList);

    while (!moveList.isEmpty()) {
      board[moveList.get(0)] = player.symbol;
      symbol = player.symbol;

      int val = minMove(board, player);

      if (val > bestVal) {
        bestVal = val;
        index = 0;
        bestMoves[index] = moveList.get(0) + 1;
      } else if (val == bestVal) {
        bestMoves[++index] = moveList.get(0) + 1;
      }

      System.out.printf("\nminimax: %3d(%1d) ", 1 + moveList.get(0), val);
      board[moveList.get(0)] = ' ';
      moveList.remove(0);
    }

    if (index > 0) {
      Random random = new Random();
      index = random.nextInt(index);
    }

    System.out.printf("\nminimax best: %3d(%1d) ", bestMoves[index], bestVal);
    System.out.printf("Steps counted: %d", q);
    q = 0;
    return bestMoves[index];
  }

  int minMove(char[] board, Player player) {
    int posValue = evaluatePosition(board, player);
    if (posValue != -1) {
      return posValue;
    }

    q++;
    int bestVal = Game.INF;
    ArrayList<Integer> moveList = new ArrayList<>();

    generateMoves(board, moveList);

    while (!moveList.isEmpty()) {
      symbol = (player.symbol == 'X') ? 'O' : 'X';
      board[moveList.get(0)] = symbol;

      int val = maxMove(board, player);

      if (val < bestVal) {
        bestVal = val;
      }
      board[moveList.get(0)] = ' ';
      moveList.remove(0);
    }
    return bestVal;
  }

  int maxMove(char[] board, Player player) {
    int posValue = evaluatePosition(board, player);
    if (posValue != -1) {
      return posValue;
    }

    q++;
    int bestVal = -Game.INF;
    ArrayList<Integer> moveList = new ArrayList<>();
    generateMoves(board, moveList);
    while (!moveList.isEmpty()) {
      symbol = (player.symbol == 'X') ? 'X' : 'O';
      board[moveList.get(0)] = symbol;
      int val = minMove(board, player);
      if (val > bestVal) {
        bestVal = val;
      }
      board[moveList.get(0)] = ' ';
      moveList.remove(0);
    }
    return bestVal;
  }
}

public class Program {
  public static FileWriter fileWriter;
  public static PrintWriter printWriter;

  public static void main(String[] args) throws IOException {
    JFrame frame = new JFrame("Demo");
    frame.add(new TicTacToePanel(new GridLayout(3, 3)));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setBounds(5, 5, 500, 500);
    frame.setVisible(true);
  }
}

class TicTacToeCell extends JButton {
  private int num;
  private int row;
  private int col;
  private char marker;

  public TicTacToeCell(int num, int x, int y) {
    this.num = num;
    row = y;
    col = x;
    marker = ' ';
    setText(Character.toString(marker));
    setFont(new Font("Arial", Font.PLAIN, 40));
  }

  public void setMarker(String markerValue) {
    marker = markerValue.charAt(0);
    setText(markerValue);
    setEnabled(false);
  }

  public char getMarker() {
    return marker;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public int getNum() {
    return num;
  }
}

class Utility {
  public static void print(char[] board) {
    System.out.println();
    for (int j = 0; j < 9; j++) {
      System.out.print(board[j] + "-");
    }
    System.out.println();
  }

  public static void print(int[] board) {
    System.out.println();
    for (int j = 0; j < 9; j++) {
      System.out.print(board[j] + "-");
    }
    System.out.println();
  }

  public static void print(ArrayList<Integer> moves) {
    System.out.println();
    for (int j = 0; j < moves.size(); j++) {
      System.out.print(moves.get(j) + "-");
    }
    System.out.println();
  }
}

class TicTacToePanel extends JPanel implements ActionListener {
  private final Game game;
  private final TicTacToeCell[] cells = new TicTacToeCell[9];

  private void createCell(int num, int x, int y) {
    cells[num] = new TicTacToeCell(num, x, y);
    cells[num].addActionListener(this);
    add(cells[num]);
  }

  TicTacToePanel(GridLayout layout) {
    super(layout);
    createCell(0, 0, 0);
    createCell(1, 1, 0);
    createCell(2, 2, 0);
    createCell(3, 0, 1);
    createCell(4, 1, 1);
    createCell(5, 2, 1);
    createCell(6, 0, 2);
    createCell(7, 1, 2);
    createCell(8, 2, 2);
    game = new Game();
    game.cplayer = game.player1;
  }

  Game getGame() {
    return game;
  }

  TicTacToeCell[] getCells() {
    return cells;
  }

  protected void finishGame(State state) {
    switch (state) {
      case XWIN:
        JOptionPane.showMessageDialog(null, "Выиграли крестики", "Результат", JOptionPane.WARNING_MESSAGE);
        System.exit(0);
        break;
      case OWIN:
        JOptionPane.showMessageDialog(null, "Выиграли нолики", "Результат", JOptionPane.WARNING_MESSAGE);
        System.exit(0);
        break;
      case DRAW:
        JOptionPane.showMessageDialog(null, "Ничья", "Результат", JOptionPane.WARNING_MESSAGE);
        System.exit(0);
        break;
      default:
        break;
    }
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    game.player1.move = -1;
    game.player2.move = -1;

    int i = 0;
    for (TicTacToeCell button : cells) {
      if (actionEvent.getSource() == button) {
        button.setMarker(Character.toString(game.cplayer.symbol));
      }
      game.board[i++] = button.getMarker();
    }

    if (game.cplayer == game.player1) {
      game.player2.move = game.miniMax(game.board, game.player2);
      game.nmove = game.player2.move;
      game.symbol = game.player2.symbol;
      game.cplayer = game.player2;
      if (game.player2.move > 0) {
        cells[game.player2.move - 1].doClick();
      }
    } else {
      game.nmove = game.player1.move;
      game.symbol = game.player1.symbol;
      game.cplayer = game.player1;
    }

    game.state = game.checkState(game.board);

    if (game.state != State.PLAYING) {
      finishGame(game.state);
    }
  }
}
