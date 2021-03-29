package com.webcheckers.model;

import com.webcheckers.application.GameController;
import com.webcheckers.util.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * A single Checkers game
 *
 * @author Danny Gardner
 */
public class CheckersGame {
    private static final Logger LOG = Logger.getLogger(CheckersGame.class.getName());

    private final Space[][] board;

    /** The side length of a square checkers board */
    public static final int BOARD_SIZE = 8;

    protected enum State {
        PLAYING,
        RESIGNED
    }

    private State state;

    private final Player redPlayer;
    private final Player whitePlayer;

    private Player winner;
    private Player loser;

    private Piece.Color activeColor;

    //******************
    //This will need to be turned into a queue that saves all of the attempted moves, since multiple
    //multiple moves can be made when jumping
    //******************
    private Move attemptedMove;

    /**
     * The CheckersGame data type
     *
     * @param redPlayer the player with the red pieces
     * @param whitePlayer the player with the white pieces
     */
    public CheckersGame(Player redPlayer, Player whitePlayer) {
        LOG.fine("Game created");
        board = new Space[BOARD_SIZE][BOARD_SIZE];

        for (int col = 0; col < board.length; col++) {
            if (col % 2 == 1) {
                board[0][col] = new Space(col, Space.State.OCCUPIED);
                board[2][col] = new Space(col, Space.State.OCCUPIED);
                board[6][col] = new Space(col, Space.State.OCCUPIED);
                board[1][col] = new Space(col, Space.State.INVALID);
                board[3][col] = new Space(col, Space.State.INVALID);
                board[5][col] = new Space(col, Space.State.INVALID);
                board[7][col] = new Space(col, Space.State.INVALID);
                board[4][col] = new Space(col, Space.State.OPEN);
            } else {
                board[1][col] = new Space(col, Space.State.OCCUPIED);
                board[5][col] = new Space(col, Space.State.OCCUPIED);
                board[7][col] = new Space(col, Space.State.OCCUPIED);
                board[0][col] = new Space(col, Space.State.INVALID);
                board[2][col] = new Space(col, Space.State.INVALID);
                board[4][col] = new Space(col, Space.State.INVALID);
                board[6][col] = new Space(col, Space.State.INVALID);
                board[3][col] = new Space(col, Space.State.OPEN);
            }
        }
        GameController.initializeBoard(board);
        this.redPlayer = redPlayer;
        this.whitePlayer = whitePlayer;
        this.activeColor = Piece.Color.RED;
        this.winner = null;
        this.loser = null;
        this.state = State.PLAYING;
    }

    /**
     * Get the board of the game
     *
     * @return the game board
     */
    public Space[][] getBoard() {
        return board;
    }

    /**
     * Get the board for the red player
     *
     * @return the game board
     */
    public BoardView getRedBoardView() {
        Row[] rows = new Row[BOARD_SIZE];
        for(int i = 0; i < BOARD_SIZE; i++) {
            rows[i] = new Row(i, board[i]);
        }
        return new BoardView(rows);
    }

    /**
     * Get the board for the white player.
     * This reverses the order of all the rows of the board from BoardView.
     *
     * @return the game board.
     */
    public BoardView getWhiteBoardView() {
        Row[] rows = new Row[BOARD_SIZE];
        for(int i = BOARD_SIZE - 1; i >= 0; i--) {
            Space[] tempSpaces = Arrays.copyOf(board[i], BOARD_SIZE);
            Collections.reverse(Arrays.asList(tempSpaces));
            rows[BOARD_SIZE - i - 1] = new Row(i, tempSpaces);
        }
        return new BoardView(rows);
    }

    /**
     * Gets the player of the red pieces
     *
     * @return the player of the red pieces
     */
    public Player getRedPlayer() {
        return redPlayer;
    }

    /**
     * Gets the player of the white pieces
     *
     * @return the player of the white pieces
     */
    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Piece.Color getActiveColor() {
        return activeColor;
    }

    //Called from PostValidateMoveRoute (and maybe backup move)
    public Message saveAttemptedMove(Move attemptedMove) {
        Position start = attemptedMove.getStart();
        Position end = attemptedMove.getEnd();
        int deltaCol = Math.abs(start.getCell() - end.getCell());
        boolean isValidMove = (deltaCol == 1) &&
                              ((activeColor == Piece.Color.RED && end.getRow() + 1 == start.getRow()) ||
                               (activeColor == Piece.Color.WHITE && end.getRow() - 1 == start.getRow()));
        if(isValidMove) {
            this.attemptedMove = attemptedMove;
            return Message.info("Valid move");
        } else {
            return Message.error("Not a valid move");
        }
    }

    //Called from GameManager when PostSubmitMoveRoute tells it to
    public Message applyAttemptedMove() {
        Position startMove = attemptedMove.getStart();
        Position endMove = attemptedMove.getEnd();
        Piece pieceBeingMoved = board[startMove.getRow()][startMove.getCell()].getPiece();
        board[startMove.getRow()][startMove.getCell()] = new Space(startMove.getCell(), Space.State.OPEN);
        board[endMove.getRow()][endMove.getCell()] = new Space(endMove.getCell(), pieceBeingMoved);
        //Flip the active color
        if(activeColor == Piece.Color.RED)
            activeColor = Piece.Color.WHITE;
        else
            activeColor = Piece.Color.RED;
        return Message.info("Move applied");
    }

    public Message resetAttemptedMove() {
        attemptedMove = null;
        return Message.info("Attempted move was removed");
    }

    public Piece.Color getPlayerColor(Player player) {
        if(player.equals(redPlayer)) {
            return Piece.Color.RED;
        }
        else if(player.equals(whitePlayer)) {
            return Piece.Color.WHITE;
        }
        else {
            return null;
        }
    }


    public boolean resignGame(Player player) {
        // If there is no active turn
        if(activeColor == null) {
            return false;
        }
        // Can only resign if it is their turn
        else if(activeColor == getPlayerColor(player)) {
            loser = player;
            state = State.RESIGNED;
            if(redPlayer.equals(player)) {
                winner = whitePlayer;
            }
            else {
                winner = redPlayer;
            }
            return true;
        }
        // Otherwise it is not their turn or something's wrong
        return false;
    }

    public boolean isResigned() {
        return state == State.RESIGNED;
    }

    public Player getWinner() {
        return this.winner;
    }

    public Player getLoser() {
        return this.loser;
    }

}
