// MinimaxPlayer.java

import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 *
 * @author Sean Bridges
 * @version 1.0
 *
 *          The MinimaxPlayer uses the minimax algorithm to determine what move
 *          it should make. Looks ahead depth moves. The number of moves will be
 *          on the order of n^depth, where n is the number of moves possible,
 *          and depth is the number of moves the engine is searching. Because of
 *          this the minimax player periodically polls the thread that calls
 *          getMove(..) to see if it was interrupted. It the thread is
 *          interrupted, the player returns null after it checks.
 */
public class MinimaxPlayer extends DefaultPlayer {

//----------------------------------------------
  // instance variables

  // the number of levels minimax will look ahead
  private int depth = 1;
  private Player minPlayer;

//----------------------------------------------
  // constructors

  /** Creates new MinimaxPlayer */
  public MinimaxPlayer(String name, int number, Player minPlayer) {
    super(name, number);

    this.minPlayer = minPlayer;
  }

//----------------------------------------------
  // instance methods

  /**
   * Get the number of levels that the Minimax Player is currently looking ahead.
   */
  public int getDepth() {
    return depth;
  }

  /**
   * Set the number of levels that the Minimax Player will look ahead when getMove
   * is next called
   */
  public void setDepth(int anInt) {
    depth = anInt;
  }

  /**
   * Passed a copy of the board, asked what move it would like to make.
   *
   * The MinimaxPlayer periodically polls the thread that makes this call to see
   * if it is interrupted. If it is the player returns null.
   *
   * Looks ahead depth moves.
   */
  public Move getMove(Board b) {
    MinimaxCalculator calc = new MinimaxCalculator(b, this, minPlayer);
    return calc.calculateMove(depth);
  }

}// end MinimaxPlayer

/**
 * The MinimaxCalculator does the actual work of finding the minimax move. A new
 * calculator should be created each time a move is to be made. A calculator
 * should only be used once.
 */
final class MinimaxCalculator {

//-------------------------------------------------------
  // instance variables

  // the number of moves we have tried
  private int moveCount = 0;
  private long startTime;

  private Player minPlayer;
  private Player maxPlayer;
  private Board board;

  private final int MAX_POSSIBLE_STRENGTH;
  private final int MIN_POSSIBLE_STRENGTH;

  //-------------------------------------------------------
  // constructors
  MinimaxCalculator(Board b, Player max, Player min) {
    board = b;
    maxPlayer = max;
    minPlayer = min;

    MAX_POSSIBLE_STRENGTH = board.getBoardStats().getMaxStrength();
    MIN_POSSIBLE_STRENGTH = board.getBoardStats().getMinStrength();
  }

//-------------------------------------------------------
  // instance methods

  /**
   * Calculate the move to be made.
   */
  public Move calculateMove(int depth) {
    startTime = System.currentTimeMillis();

    if (depth == 0) {
      System.out.println("Error, 0 depth in minumax player");
      Thread.dumpStack();
      return null;
    }

    Move[] moves = board.getPossibleMoves(maxPlayer);
    int maxStrength = MIN_POSSIBLE_STRENGTH;
    int maxIndex = 0;

    for (int i : new int[]{3, 4, 2, 5, 1, 6, 0, 7}) {
      if (Arrays.stream(moves).anyMatch(m -> m.toInt() == i)) {

        if (board.move(moves[i])) {
          moveCount++;

          int strength = expandMinNode(MIN_POSSIBLE_STRENGTH, MAX_POSSIBLE_STRENGTH, depth - 1);
          if (strength > maxStrength) {
            maxStrength = strength;
            maxIndex = i;
          }
          board.undoLastMove();
        } // end if move made

        // if the thread has been interrupted, return immediately.
        if (Thread.currentThread().isInterrupted()) {
          return null;
        }

      }
    } // end for all moves

    long stopTime = System.currentTimeMillis();
    System.out.print("MINIMAX: Number of moves tried:" + moveCount);
    System.out.println(" Time:" + (stopTime - startTime) + " milliseconds");

    return moves[maxIndex];

  }

  /**
   * A max node returns the max score of its descendents. parentMinimum is the
   * minumum score that the parent has already encountered. if we find a score
   * that is higher than this, we will return that score immediately rather than
   * continue to expand the tree, since the min node above us only cares if we are
   * lower than its current min score.
   */
  private int expandMaxNode(int alpha, int beta, int depth) {
    // base step
    if (depth == 0 || board.isGameOver()) {
      return board.getBoardStats().getStrength(maxPlayer);
    }

    // recursive step
    Move[] moves = board.getPossibleMoves(maxPlayer);
    int maxStrength = MIN_POSSIBLE_STRENGTH;

    for (int i : new int[]{3, 4, 2, 5, 1, 6, 0, 7}) {
      if (Arrays.stream(moves).anyMatch(m -> m.toInt() == i)) {
        if (board.move(moves[i])) {
          moveCount++;
          alpha = max(alpha, expandMinNode(alpha, beta, depth - 1));

          if (alpha >= beta) {
            board.undoLastMove();
            return alpha;
          }
          if (alpha > maxStrength) {
            maxStrength = alpha;
          }
          board.undoLastMove();
        } // end if move made

      }
    } // end for all moves

    return maxStrength;

  }// end expandMaxNode

  /**
   * The min node chooses the smallest of its descendents. parentMaximum is the
   * maximum that the parent max node has already found, if we find something
   * smaller than this, return immediatly, since the parent max node will choose
   * the greatest value it can find.
   */
  private int expandMinNode(int alpha, int beta, int depth) {
    // base step
    if (depth == 0 || board.isGameOver()) {
      return board.getBoardStats().getStrength(maxPlayer);
    }

    // recursive step
    Move[] moves = board.getPossibleMoves(minPlayer);
    int minStrength = MAX_POSSIBLE_STRENGTH;

    for (int i : new int[]{3, 4, 2, 5, 1, 6, 0, 7}) {
      if (Arrays.stream(moves).anyMatch(m -> m.toInt() == i)) {
        if (board.move(moves[i])) {
          moveCount++;
          beta = min(beta, expandMaxNode(alpha, beta, depth - 1));

          if (beta <= alpha) {
            board.undoLastMove();
            return beta;
          }
          if (beta < minStrength) {
            minStrength = beta;
          }
          board.undoLastMove();
        } // end if move made

      }
    } // end for all moves

    return minStrength;

  }// end expandMaxNode

}